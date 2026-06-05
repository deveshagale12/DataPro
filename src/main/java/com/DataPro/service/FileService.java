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
 
        // SHA-256 hash for dedup
        String hash;
        try {
            hash = encryptionService.computeSHA256(rawBytes);
        } catch (Exception e) {
            log.error("[UPLOAD] SHA-256 failed for userId={}", userId);
            throw new EncryptionException("Failed to compute file hash: " + e.getMessage());
        }
        log.info("[UPLOAD] SHA-256 hash={}", hash);
 
        // Duplicate check
        if (fileRepo.existsByFileHash(hash)) {
            log.warn("[UPLOAD] Duplicate detected userId={} hash={}", userId, hash);
            throw new DuplicateFileException("This file already exists (duplicate content detected by SHA-256).");
        }
 
        // AES-256 encrypt
        byte[] encryptedBytes;
        try {
            encryptedBytes = encryptionService.encrypt(rawBytes);
        } catch (Exception e) {
            log.error("[UPLOAD] Encryption failed userId={}", userId);
            throw new EncryptionException("File encryption failed: " + e.getMessage());
        }
        log.info("[UPLOAD] File encrypted userId={}", userId);
 
        // Upload to Supabase
        String supabasePath = "user_" + userId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        String supabaseUrl;
        try {
            supabaseUrl = supabaseService.uploadFile(supabasePath, encryptedBytes, file.getContentType());
        } catch (Exception e) {
            log.error("[UPLOAD] Supabase upload failed userId={}: {}", userId, e.getMessage());
            throw new StorageException("Failed to store file in Supabase: " + e.getMessage());
        }
        log.info("[UPLOAD] Stored in Supabase path={}", supabasePath);
 
        // Save metadata to DB
        UserFile userFile = new UserFile();
        userFile.setFileName(file.getOriginalFilename());
        userFile.setFileHash(hash);
        userFile.setUserId(userId);
        userFile.setContentType(file.getContentType());
        userFile.setSupabasePath(supabasePath);
        userFile.setSupabaseUrl(supabaseUrl);
        userFile.setPublic(isPublic);
        fileRepo.save(userFile);
        log.info("[UPLOAD] Metadata saved fileId={}", userFile.getId());
 
        saveAuditLog(userFile.getId(), file.getOriginalFilename(), uploaderEmail, "UPLOAD", userId);
        emailService.sendUploadSuccessEmail(uploaderEmail, file.getOriginalFilename());
 
        return toResponse(userFile);
    }
 
    // ── MY FILES ──────────────────────────────────────────────────────
    public List<FileDTO.FileResponse> getMyFiles(Long userId) {
        log.info("[MY-FILES] userId={}", userId);
        List<UserFile> files = fileRepo.findByUserId(userId);
        log.info("[MY-FILES] Found {} files for userId={}", files.size(), userId);
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── COMMUNITY FILES ───────────────────────────────────────────────
    public List<FileDTO.FileResponse> getCommunityFiles(Long userId) {
        log.info("[COMMUNITY-FILES] requestedByUserId={}", userId);
        List<UserFile> files = fileRepo.findByIsPublicTrue();
        log.info("[COMMUNITY-FILES] Found {} public files", files.size());
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── GET ALL FILES (ADMIN) ─────────────────────────────────────────
    public List<FileDTO.FileResponse> getAllFiles() {
        log.info("[ALL-FILES] Fetching all files from DB");
        List<UserFile> files = fileRepo.findAll();
        log.info("[ALL-FILES] Total files in system={}", files.size());
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── GET FILE BY ID ────────────────────────────────────────────────
    public FileDTO.FileResponse getFileById(Long fileId) {
        log.info("[FILE-BY-ID] fileId={}", fileId);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
        return toResponse(file);
    }
 
    // ── DECRYPT AND VIEW (inline - for images/pdf/text) ───────────────
    public byte[] decryptAndView(Long fileId, String requesterEmail, String otp) throws Exception {
        log.info("[DECRYPT-VIEW] fileId={} requester={}", fileId, requesterEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        if (file.isAccessRevoked()) {
            log.warn("[DECRYPT-VIEW] Access revoked fileId={}", fileId);
            throw new AccessDeniedException("Access to this file has been revoked.");
        }
 
        // OTP check — if file has OTP set, validate it; owner can skip with null otp
        if (file.getShareOtp() != null) {
            if (otp == null || otp.isBlank()) {
                log.warn("[DECRYPT-VIEW] OTP required but not provided fileId={}", fileId);
                throw new InvalidOtpException("This file requires an OTP to view.");
            }
            if (!file.getShareOtp().equals(otp)) {
                log.warn("[DECRYPT-VIEW] Invalid OTP fileId={} requester={}", fileId, requesterEmail);
                throw new InvalidOtpException("Invalid OTP provided.");
            }
            if (file.getOtpCreatedAt() == null ||
                    file.getOtpCreatedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
                log.warn("[DECRYPT-VIEW] Expired OTP fileId={}", fileId);
                throw new InvalidOtpException("OTP has expired. Please request a new one.");
            }
        }
 
        // Download encrypted bytes from Supabase
        byte[] encryptedBytes;
        try {
            encryptedBytes = supabaseService.downloadFile(file.getSupabasePath());
        } catch (Exception e) {
            log.error("[DECRYPT-VIEW] Supabase download failed: {}", e.getMessage());
            throw new StorageException("Failed to retrieve file from storage: " + e.getMessage());
        }
 
        // Decrypt
        byte[] decryptedBytes;
        try {
            decryptedBytes = encryptionService.decrypt(encryptedBytes);
        } catch (Exception e) {
            log.error("[DECRYPT-VIEW] Decryption failed fileId={}", fileId);
            throw new EncryptionException("File decryption failed: " + e.getMessage());
        }
 
        log.info("[DECRYPT-VIEW] File decrypted and ready fileId={} requester={}", fileId, requesterEmail);
        saveAuditLog(fileId, file.getFileName(), requesterEmail, "VIEW", file.getUserId());
        return decryptedBytes;
    }
 
    // ── DECRYPT AND DOWNLOAD ──────────────────────────────────────────
    public byte[] decryptAndDownload(Long fileId, String requesterEmail, String otp) throws Exception {
        log.info("[DECRYPT-DOWNLOAD] fileId={} requester={}", fileId, requesterEmail);
 
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found with id: " + fileId));
 
        if (file.isAccessRevoked()) {
            throw new AccessDeniedException("Access to this file has been revoked.");
        }
 
        if (file.getShareOtp() != null) {
            if (otp == null || otp.isBlank()) throw new InvalidOtpException("OTP required.");
            if (!file.getShareOtp().equals(otp)) throw new InvalidOtpException("Invalid OTP.");
            if (file.getOtpCreatedAt() == null ||
                    file.getOtpCreatedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
                throw new InvalidOtpException("OTP expired. Request a new one.");
            }
        }
 
        byte[] encryptedBytes;
        try {
            encryptedBytes = supabaseService.downloadFile(file.getSupabasePath());
        } catch (Exception e) {
            throw new StorageException("Failed to retrieve file: " + e.getMessage());
        }
 
        byte[] decryptedBytes;
        try {
            decryptedBytes = encryptionService.decrypt(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("File decryption failed: " + e.getMessage());
        }
 
        log.info("[DECRYPT-DOWNLOAD] File ready for download fileId={}", fileId);
        saveAuditLog(fileId, file.getFileName(), requesterEmail, "DOWNLOAD", file.getUserId());
        return decryptedBytes;
    }
 
    // ── FILE STATS ────────────────────────────────────────────────────
    public FileDTO.FileStatsResponse getFileStats(Long userId) {
        log.info("[FILE-STATS] userId={}", userId);
        List<UserFile> myFiles = fileRepo.findByUserId(userId);
        long publicCount  = myFiles.stream().filter(UserFile::isPublic).count();
        long privateCount = myFiles.size() - publicCount;
        long downloads    = logRepo.findByOwnerIdOrderByDownloadTimeDesc(userId).stream()
                .filter(l -> "DOWNLOAD".equals(l.getAction())).count();
 
        FileDTO.FileStatsResponse stats = new FileDTO.FileStatsResponse();
        stats.setTotalFiles(myFiles.size());
        stats.setPublicFiles(publicCount);
        stats.setPrivateFiles(privateCount);
        stats.setTotalDownloads(downloads);
        log.info("[FILE-STATS] total={} public={} private={} downloads={}", myFiles.size(), publicCount, privateCount, downloads);
        return stats;
    }
 
    // ── SEND OTP ──────────────────────────────────────────────────────
    public void sendOtp(Long fileId, String targetEmail, String ownerEmail) {
        log.info("[OTP-SEND] fileId={} targetEmail={}", fileId, targetEmail);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
 
        String otp = generateOtp();
        file.setShareOtp(otp);
        file.setSharedWithEmail(targetEmail);
        file.setOtpCreatedAt(LocalDateTime.now());
        fileRepo.save(file);
 
        emailService.sendOtpEmail(targetEmail, otp, file.getFileName());
        log.info("[OTP-SEND] OTP queued for {} file={}", targetEmail, file.getFileName());
        saveAuditLog(fileId, file.getFileName(), targetEmail, "OTP_SENT", file.getUserId());
    }
 
    // ── REQUEST ACCESS ────────────────────────────────────────────────
    public void requestAccess(Long fileId, String requesterEmail, String ownerEmail) {
        log.info("[REQUEST-ACCESS] fileId={} requester={}", fileId, requesterEmail);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
 
        Optional<AccessRequest> existing = accessRepo.findByFileIdAndRequesterEmail(fileId, requesterEmail);
        if (existing.isPresent() && "PENDING".equals(existing.get().getStatus())) {
            throw new AccessDeniedException("Access request already pending.");
        }
 
        AccessRequest req = new AccessRequest();
        req.setFileId(fileId);
        req.setRequesterEmail(requesterEmail);
        req.setOwnerId(file.getUserId());
        req.setStatus("PENDING");
        req.setRequestedAt(LocalDateTime.now());
        accessRepo.save(req);
 
        emailService.sendAccessRequestEmail(ownerEmail, requesterEmail, file.getFileName());
        saveAuditLog(fileId, file.getFileName(), requesterEmail, "ACCESS_REQUESTED", file.getUserId());
        log.info("[REQUEST-ACCESS] Saved and owner notified");
    }
 
    // ── PENDING REQUESTS ──────────────────────────────────────────────
    public List<AccessRequest> getPendingRequests(Long ownerId) {
        log.info("[PENDING-REQUESTS] ownerId={}", ownerId);
        List<AccessRequest> list = accessRepo.findByOwnerIdAndStatus(ownerId, "PENDING");
        log.info("[PENDING-REQUESTS] Found {}", list.size());
        return list;
    }
 
    // ── REVOKE ACCESS ─────────────────────────────────────────────────
    public void revokeAccess(Long fileId, String ownerEmail) {
        log.info("[REVOKE-ACCESS] fileId={}", fileId);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
 
        String revokedEmail = file.getSharedWithEmail();
        file.setAccessRevoked(true);
        file.setShareOtp(null);
        file.setSharedWithEmail(null);
        file.setOtpCreatedAt(null);
        fileRepo.save(file);
 
        if (revokedEmail != null) emailService.sendRevokeNotificationEmail(revokedEmail, file.getFileName());
        saveAuditLog(fileId, file.getFileName(), revokedEmail, "ACCESS_REVOKED", file.getUserId());
        log.info("[REVOKE-ACCESS] Done fileId={}", fileId);
    }
 
    // ── SHARE FILE ────────────────────────────────────────────────────
    public void shareFile(Long fileId, String sharedWithEmail, String ownerEmail) {
        log.info("[SHARE-FILE] fileId={} sharedWith={}", fileId, sharedWithEmail);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
 
        String otp = generateOtp();
        file.setShareOtp(otp);
        file.setSharedWithEmail(sharedWithEmail);
        file.setOtpCreatedAt(LocalDateTime.now());
        file.setAccessRevoked(false);
        fileRepo.save(file);
 
        emailService.sendOtpEmail(sharedWithEmail, otp, file.getFileName());
        saveAuditLog(fileId, file.getFileName(), sharedWithEmail, "FILE_SHARED", file.getUserId());
        log.info("[SHARE-FILE] OTP sent to {}", sharedWithEmail);
    }
 
    // ── VERIFY OTP + DOWNLOAD ─────────────────────────────────────────
    public byte[] verifyAndDownload(Long fileId, String otp,
                                     String requesterEmail, String ownerEmail) throws Exception {
        log.info("[VERIFY-DOWNLOAD] fileId={} requester={}", fileId, requesterEmail);
        return decryptAndDownload(fileId, requesterEmail, otp);
    }
 
    // ── AUDIT LOGS ────────────────────────────────────────────────────
    public List<FileDTO.AuditLogResponse> getAuditLogs(Long userId) {
        log.info("[AUDIT-LOGS] userId={}", userId);
        List<DownloadLog> logs = logRepo.findByOwnerIdOrderByDownloadTimeDesc(userId);
        log.info("[AUDIT-LOGS] Found {} entries", logs.size());
        return logs.stream().map(this::toAuditResponse).collect(Collectors.toList());
    }
 
    // ── SHARED WITH ME ────────────────────────────────────────────────
    public List<FileDTO.FileResponse> getSharedWithMe(String email) {
        log.info("[SHARED-WITH-ME] email={}", email);
        List<UserFile> files = fileRepo.findBySharedWithEmail(email);
        log.info("[SHARED-WITH-ME] Found {} files", files.size());
        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }
 
    // ── DELETE FILE ───────────────────────────────────────────────────
    public void deleteFile(Long fileId, String ownerEmail) {
        log.info("[DELETE-FILE] fileId={} owner={}", fileId, ownerEmail);
        UserFile file = fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
 
        try {
            supabaseService.deleteFile(file.getSupabasePath());
        } catch (Exception e) {
            log.warn("[DELETE-FILE] Supabase delete failed, removing DB record anyway: {}", e.getMessage());
        }
 
        fileRepo.delete(file);
        saveAuditLog(fileId, file.getFileName(), ownerEmail, "DELETED", file.getUserId());
        log.info("[DELETE-FILE] Deleted fileId={}", fileId);
    }
 
    // ── HELPERS ───────────────────────────────────────────────────────
    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }
 
    private void saveAuditLog(Long fileId, String fileName, String email, String action, Long ownerId) {
        DownloadLog dl = new DownloadLog();
        dl.setFileId(fileId);
        dl.setFileName(fileName);
        dl.setDownloaderEmail(email);
        dl.setAction(action);
        dl.setDownloadTime(LocalDateTime.now());
        dl.setOwnerId(ownerId);
        logRepo.save(dl);
        log.info("[AUDIT] action={} fileId={} by={}", action, fileId, email);
    }
 
    public UserFile getRawFile(Long fileId) {
        return fileRepo.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileId));
    }
 
    private FileDTO.FileResponse toResponse(UserFile f) {
        FileDTO.FileResponse r = new FileDTO.FileResponse();
        r.setId(f.getId());
        r.setFileName(f.getFileName());
        r.setContentType(f.getContentType());
        r.setUserId(f.getUserId());
        r.setPublic(f.isPublic());
        r.setSupabaseUrl(f.getSupabaseUrl());
        r.setFileHash(f.getFileHash());
        r.setOtpCreatedAt(f.getOtpCreatedAt());
        r.setAccessRevoked(f.isAccessRevoked());
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