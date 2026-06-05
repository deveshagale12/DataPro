package com.DataPro.service;


import com.DataPro.dto.FileDTO;
import com.DataPro.entity.AccessRequest;
import com.DataPro.entity.DownloadLog;
import com.DataPro.entity.UserFile;
import com.DataPro.exception.*;
import com.DataPro.repository.AccessRequestRepository;
import com.DataPro.repository.DownloadLogRepository;
import com.DataPro.repository.UserFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class FileService {
 
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final int OTP_EXPIRY_MINUTES = 10;
 
    private final UserFileRepository fileRepo;
    private final DownloadLogRepository logRepo;
    private final AccessRequestRepository accessRepo;
    private final EncryptionService encryptionService;
    private final SupabaseStorageService supabaseService;
    private final EmailService emailService;
 
    public FileService(UserFileRepository fileRepo,
                       DownloadLogRepository logRepo,
                       AccessRequestRepository accessRepo,
                       EncryptionService encryptionService,
                       SupabaseStorageService supabaseService,
                       EmailService emailService) {
        this.fileRepo = fileRepo;
        this.logRepo = logRepo;
        this.accessRepo = accessRepo;
        this.encryptionService = encryptionService;
        this.supabaseService = supabaseService;
        this.emailService = emailService;
    }
 
    // ── UPLOAD SECURE ─────────────────────────────────────────────────
    public FileDTO.FileResponse uploadSecure(MultipartFile file, Long userId,
                                              String uploaderEmail, boolean isPublic) throws Exception {
        log.info("[UPLOAD] userId={} file={} size={} bytes", userId, file.getOriginalFilename(), file.getSize());
 
        byte[] rawBytes = file.getBytes();
 
        // 1. SHA-256 hash of raw content for dedup check
        String hash;
        try {
            hash = encryptionService.computeSHA256(rawBytes);
        } catch (Exception e) {
            log.error("[UPLOAD] SHA-256 computation failed for userId={}", userId);
            throw new EncryptionException("Failed to compute file hash: " + e.getMessage());
        }
        log.info("[UPLOAD] SHA-256 hash computed: {}", hash);
 
        // 2. Duplicate check by hash
        if (fileRepo.existsByFileHash(hash)) {
            log.warn("[UPLOAD] Duplicate file detected for userId={} hash={}", userId, hash);
            throw new DuplicateFileException("This file already exists in the system (duplicate content detected by SHA-256).");
        }
 
        // 3. AES-256 encrypt
        byte[] encryptedBytes;
        try {
            encryptedBytes = encryptionService.encrypt(rawBytes);
        } catch (Exception e) {
            log.error("[UPLOAD] AES encryption failed for userId={}", userId);
            throw new EncryptionException("File encryption failed: " + e.getMessage());
        }
        log.info("[UPLOAD] File encrypted successfully for userId={}", userId);
 
        // 4. Upload to Supabase
        String supabasePath = "user_" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String supabaseUrl;
        try {
            supabaseUrl = supabaseService.uploadFile(supabasePath, encryptedBytes, file.getContentType());
        } catch (Exception e) {
            log.error("[UPLOAD] Supabase upload failed for userId={}: {}", userId, e.getMessage());
            throw new StorageException("Failed to store file in Supabase: " + e.getMessage());
        }
        log.info("[UPLOAD] File stored in Supabase at path={}", supabasePath);
 
        // 5. Save metadata to DB
        UserFile userFile = new UserFile();
        userFile.setFileName(file.getOriginalFilename());
        userFile.setFileHash(hash);
        userFile.setUserId(userId);
        userFile.setContentType(file.getContentType());
        userFile.setSupabasePath(supabasePath);
        userFile.setSupabaseUrl(supabaseUrl);
        userFile.setPublic(isPublic);
        fileRepo.save(userFile);
        log.info("[UPLOAD] Metadata saved to DB. fileId={}", userFile.getId());
 
        // 6. Audit log
        saveAuditLog(userFile.getId(), file.getOriginalFilename(), uploaderEmail, "UPLOAD", userId);
 
        // 7. Send email async — no delay to response
        emailService.sendUploadSuccessEmail(uploaderEmail, file.getOriginalFilename());
        log.info("[UPLOAD] Upload success email queued for {}", uploaderEmail);
 
        return toResponse(userFile);
    }
 
    // ── MY FILES ──────────────────────────────────────────────────────
    public List<FileDTO.FileResponse> getMyFiles(Long userId) {
        log.info("[MY-FILES] Fetching files for userId={}", userId);
        List<UserFile> files = fileRepo.findByUserId(userId);
        log.info("[MY-FILES] Found {} files for userId={}", files.size(), userId);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── COMMUNITY FILES ───────────────────────────────────────────────
    public List<FileDTO.FileResponse> getCommunityFiles(Long userId) {
        log.info("[COMMUNITY-FILES] Fetching public files. requestedByUserId={}", userId);
        List<UserFile> files = fileRepo.findByIsPublicTrue();
        log.info("[COMMUNITY-FILES] Found {} public files", files.size());
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── SEND OTP ──────────────────────────────────────────────────────
    public void sendOtp(Long fileId, String targetEmail, String ownerEmail) {
        log.info("[OTP-SEND] fileId={} targetEmail={}", fileId, targetEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        String otp = generateOtp();
        file.setShareOtp(otp);
        file.setSharedWithEmail(targetEmail);
        file.setOtpCreatedAt(LocalDateTime.now());
        fileRepo.save(file);
 
        emailService.sendOtpEmail(targetEmail, otp, file.getFileName());
        log.info("[OTP-SEND] OTP email queued for {} for file={}", targetEmail, file.getFileName());
 
        saveAuditLog(fileId, file.getFileName(), targetEmail, "OTP_SENT", file.getUserId());
    }
 
    // ── REQUEST ACCESS ────────────────────────────────────────────────
    public void requestAccess(Long fileId, String requesterEmail, String ownerEmail) {
        log.info("[REQUEST-ACCESS] fileId={} requester={}", fileId, requesterEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        // Check if request already exists
        Optional<AccessRequest> existing = accessRepo.findByFileIdAndRequesterEmail(fileId, requesterEmail);
        if (existing.isPresent() && "PENDING".equals(existing.get().getStatus())) {
            log.warn("[REQUEST-ACCESS] Already pending for fileId={} requester={}", fileId, requesterEmail);
            throw new AccessDeniedException("Access request already pending for this file.");
        }
 
        AccessRequest req = new AccessRequest();
        req.setFileId(fileId);
        req.setRequesterEmail(requesterEmail);
        req.setOwnerId(file.getUserId());
        req.setStatus("PENDING");
        req.setRequestedAt(LocalDateTime.now());
        accessRepo.save(req);
        log.info("[REQUEST-ACCESS] Request saved. Notifying owner at {}", ownerEmail);
 
        emailService.sendAccessRequestEmail(ownerEmail, requesterEmail, file.getFileName());
        saveAuditLog(fileId, file.getFileName(), requesterEmail, "ACCESS_REQUESTED", file.getUserId());
    }
 
    // ── PENDING REQUESTS ──────────────────────────────────────────────
    public List<AccessRequest> getPendingRequests(Long ownerId) {
        log.info("[PENDING-REQUESTS] Fetching pending requests for ownerId={}", ownerId);
        List<AccessRequest> list = accessRepo.findByOwnerIdAndStatus(ownerId, "PENDING");
        log.info("[PENDING-REQUESTS] Found {} pending requests", list.size());
        return list;
    }
 
    // ── REVOKE ACCESS ─────────────────────────────────────────────────
    public void revokeAccess(Long fileId, String ownerEmail) {
        log.info("[REVOKE-ACCESS] fileId={} owner={}", fileId, ownerEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        String revokedEmail = file.getSharedWithEmail();
        file.setAccessRevoked(true);
        file.setShareOtp(null);
        file.setSharedWithEmail(null);
        file.setOtpCreatedAt(null);
        fileRepo.save(file);
        log.info("[REVOKE-ACCESS] Access revoked for fileId={}", fileId);
 
        if (revokedEmail != null) {
            emailService.sendRevokeNotificationEmail(revokedEmail, file.getFileName());
        }
        saveAuditLog(fileId, file.getFileName(), revokedEmail, "ACCESS_REVOKED", file.getUserId());
    }
 
    // ── SHARE FILE ────────────────────────────────────────────────────
    public void shareFile(Long fileId, String sharedWithEmail, String ownerEmail) {
        log.info("[SHARE-FILE] fileId={} sharedWith={}", fileId, sharedWithEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        String otp = generateOtp();
        file.setShareOtp(otp);
        file.setSharedWithEmail(sharedWithEmail);
        file.setOtpCreatedAt(LocalDateTime.now());
        file.setAccessRevoked(false);
        fileRepo.save(file);
 
        emailService.sendOtpEmail(sharedWithEmail, otp, file.getFileName());
        log.info("[SHARE-FILE] OTP sent to {} for file={}", sharedWithEmail, file.getFileName());
 
        saveAuditLog(fileId, file.getFileName(), sharedWithEmail, "FILE_SHARED", file.getUserId());
    }
 
    // ── VERIFY OTP AND DOWNLOAD ───────────────────────────────────────
    public byte[] verifyAndDownload(Long fileId, String otp,
                                     String requesterEmail, String ownerEmail) throws Exception {
        log.info("[VERIFY-DOWNLOAD] fileId={} requester={}", fileId, requesterEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        if (file.isAccessRevoked()) {
            log.warn("[VERIFY-DOWNLOAD] Access revoked for fileId={}", fileId);
            throw new AccessDeniedException("Access to this file has been revoked.");
        }
 
        if (file.getShareOtp() == null || !file.getShareOtp().equals(otp)) {
            log.warn("[VERIFY-DOWNLOAD] Invalid OTP for fileId={} requester={}", fileId, requesterEmail);
            throw new InvalidOtpException("Invalid OTP provided.");
        }
 
        if (file.getOtpCreatedAt() == null ||
                file.getOtpCreatedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            log.warn("[VERIFY-DOWNLOAD] Expired OTP for fileId={}", fileId);
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }
 
        // Download encrypted bytes from Supabase
        byte[] encryptedBytes;
        try {
            encryptedBytes = supabaseService.downloadFile(file.getSupabasePath());
        } catch (Exception e) {
            log.error("[VERIFY-DOWNLOAD] Supabase download failed: {}", e.getMessage());
            throw new StorageException("Failed to retrieve file from storage: " + e.getMessage());
        }
 
        // Decrypt
        byte[] decryptedBytes;
        try {
            decryptedBytes = encryptionService.decrypt(encryptedBytes);
        } catch (Exception e) {
            log.error("[VERIFY-DOWNLOAD] Decryption failed for fileId={}", fileId);
            throw new EncryptionException("File decryption failed: " + e.getMessage());
        }
 
        log.info("[VERIFY-DOWNLOAD] File decrypted and ready. fileId={} requester={}", fileId, requesterEmail);
 
        // Audit + notify owner
        saveAuditLog(fileId, file.getFileName(), requesterEmail, "DOWNLOAD", file.getUserId());
        emailService.sendDownloadNotificationEmail(ownerEmail, requesterEmail, file.getFileName());
 
        return decryptedBytes;
    }
 
    // ── AUDIT LOGS ────────────────────────────────────────────────────
    public List<FileDTO.AuditLogResponse> getAuditLogs(Long userId) {
        log.info("[AUDIT-LOGS] Fetching logs for userId={}", userId);
        List<DownloadLog> logs = logRepo.findByOwnerIdOrderByDownloadTimeDesc(userId);
        log.info("[AUDIT-LOGS] Found {} log entries for userId={}", logs.size(), userId);
        return logs.stream().map(this::toAuditResponse).collect(Collectors.toList());
    }
 
    // ── SHARED WITH ME ────────────────────────────────────────────────
    public List<FileDTO.FileResponse> getSharedWithMe(String email) {
        log.info("[SHARED-WITH-ME] Fetching files shared with email={}", email);
        List<UserFile> files = fileRepo.findBySharedWithEmail(email);
        log.info("[SHARED-WITH-ME] Found {} files shared with {}", files.size(), email);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── HELPERS ───────────────────────────────────────────────────────
    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }
 
    private void saveAuditLog(Long fileId, String fileName, String downloaderEmail,
                               String action, Long ownerId) {
        DownloadLog dl = new DownloadLog();
        dl.setFileId(fileId);
        dl.setFileName(fileName);
        dl.setDownloaderEmail(downloaderEmail);
        dl.setAction(action);
        dl.setDownloadTime(LocalDateTime.now());
        dl.setOwnerId(ownerId);
        logRepo.save(dl);
        log.info("[AUDIT] action={} fileId={} by={}", action, fileId, downloaderEmail);
    }
 
    private FileDTO.FileResponse toResponse(UserFile f) {
        FileDTO.FileResponse r = new FileDTO.FileResponse();
        r.setId(f.getId());
        r.setFileName(f.getFileName());
        r.setContentType(f.getContentType());
        r.setUserId(f.getUserId());
        r.setPublic(f.isPublic());
        r.setSupabaseUrl(f.getSupabaseUrl());
        r.setOtpCreatedAt(f.getOtpCreatedAt());
        return r;
    }
 
    private FileDTO.AuditLogResponse toAuditResponse(DownloadLog dl) {
        FileDTO.AuditLogResponse r = new FileDTO.AuditLogResponse();
        r.setId(dl.getId());
        r.setFileId(dl.getFileId());
        r.setFileName(dl.getFileName());
        r.setDownloaderEmail(dl.getDownloaderEmail());
        r.setAction(dl.getAction());
        r.setDownloadTime(dl.getDownloadTime());
        return r;
    }
}
 