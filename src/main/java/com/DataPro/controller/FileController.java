package com.DataPro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.DataPro.dto.FileDTO;
import com.DataPro.entity.AccessRequest;
import com.DataPro.service.FileService;
import java.util.List;
import java.util.Map;
import com.DataPro.dto.FileDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;

 @RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
 
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
 
    private final FileService fileService;
 
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/upload-secure
    // Upload any file — AES-256 encrypt, SHA-256 dedup, store Supabase,
    // email sent on success
    // ─────────────────────────────────────────────────────────────────
    @PostMapping(value = "/upload-secure", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDTO.ApiResponse> uploadSecure(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("uploaderEmail") String uploaderEmail,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic) throws Exception {
 
        log.info("[CONTROLLER] POST /upload-secure userId={} file={} isPublic={}", userId, file.getOriginalFilename(), isPublic);
        FileDTO.FileResponse response = fileService.uploadSecure(file, userId, uploaderEmail, isPublic);
        log.info("[CONTROLLER] Upload success fileId={}", response.getId());
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "File uploaded, encrypted and stored successfully.", response));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/my-files/{userId}
    // Get all files uploaded by a specific user
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/my-files/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getMyFiles(@PathVariable Long userId) {
        log.info("[CONTROLLER] GET /my-files/{}", userId);
        List<FileDTO.FileResponse> files = fileService.getMyFiles(userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Files fetched successfully.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/community-files/{userId}
    // All public/community files visible to any user
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/community-files/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getCommunityFiles(@PathVariable Long userId) {
        log.info("[CONTROLLER] GET /community-files/{}", userId);
        List<FileDTO.FileResponse> files = fileService.getCommunityFiles(userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Community files fetched.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/all
    // ADMIN — fetch ALL files in the system
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<FileDTO.ApiResponse> getAllFiles() {
        log.info("[CONTROLLER] GET /all");
        List<FileDTO.FileResponse> files = fileService.getAllFiles();
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "All files fetched.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/{fileId}
    // Get metadata of a single file by ID
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/{fileId}")
    public ResponseEntity<FileDTO.ApiResponse> getFileById(@PathVariable Long fileId) {
        log.info("[CONTROLLER] GET /files/{}", fileId);
        FileDTO.FileResponse file = fileService.getFileById(fileId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "File found.", file));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/decrypt/view/{fileId}
    // Decrypt file and serve INLINE for browser preview (images, PDF, text)
    // Pass OTP in body if file is OTP-protected; leave blank if owner
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/decrypt/view/{fileId}")
    public ResponseEntity<byte[]> decryptAndView(
            @PathVariable Long fileId,
            @RequestBody FileDTO.DecryptRequest request) throws Exception {
 
        log.info("[CONTROLLER] POST /decrypt/view/{} requester={}", fileId, request.getRequesterEmail());
 
        byte[] decryptedBytes = fileService.decryptAndView(fileId, request.getRequesterEmail(), request.getOtp());
        UserFile raw = fileService.getRawFile(fileId);
 
        log.info("[CONTROLLER] Serving inline view fileId={} contentType={}", fileId, raw.getContentType());
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + raw.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(
                        raw.getContentType() != null ? raw.getContentType() : "application/octet-stream"))
                .body(decryptedBytes);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/decrypt/download/{fileId}
    // Decrypt file and force DOWNLOAD to client
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/decrypt/download/{fileId}")
    public ResponseEntity<byte[]> decryptAndDownload(
            @PathVariable Long fileId,
            @RequestBody FileDTO.DecryptRequest request) throws Exception {
 
        log.info("[CONTROLLER] POST /decrypt/download/{} requester={}", fileId, request.getRequesterEmail());
 
        byte[] decryptedBytes = fileService.decryptAndDownload(fileId, request.getRequesterEmail(), request.getOtp());
        UserFile raw = fileService.getRawFile(fileId);
 
        log.info("[CONTROLLER] Sending download fileId={}", fileId);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + raw.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(decryptedBytes);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/stats/{userId}
    // File statistics for a user (total, public, private, downloads)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/stats/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getFileStats(@PathVariable Long userId) {
        log.info("[CONTROLLER] GET /stats/{}", userId);
        FileDTO.FileStatsResponse stats = fileService.getFileStats(userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Stats fetched.", stats));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/otp/send
    // Owner sends OTP to a specific email to grant file access
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/otp/send")
    public ResponseEntity<FileDTO.ApiResponse> sendOtp(
            @RequestBody FileDTO.OtpRequest request,
            @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /otp/send fileId={} targetEmail={}", request.getFileId(), request.getTargetEmail());
        fileService.sendOtp(request.getFileId(), request.getTargetEmail(), ownerEmail);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "OTP sent to " + request.getTargetEmail() + " successfully."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/request-access
    // Any user requests access to a community/public file
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/request-access")
    public ResponseEntity<FileDTO.ApiResponse> requestAccess(
            @RequestBody FileDTO.AccessRequestDTO request,
            @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /request-access fileId={} requester={}", request.getFileId(), request.getRequesterEmail());
        fileService.requestAccess(request.getFileId(), request.getRequesterEmail(), ownerEmail);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Access request submitted. Owner notified."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/pending-requests/{userId}
    // Owner sees all pending access requests for their files
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/pending-requests/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getPendingRequests(@PathVariable Long userId) {
        log.info("[CONTROLLER] GET /pending-requests/{}", userId);
        List<AccessRequest> pending = fileService.getPendingRequests(userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Pending requests fetched.", pending));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // PUT /api/files/revoke-access/{fileId}
    // Owner revokes access — notifies user by email
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/revoke-access/{fileId}")
    public ResponseEntity<FileDTO.ApiResponse> revokeAccess(
            @PathVariable Long fileId,
            @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] PUT /revoke-access/{} ownerEmail={}", fileId, ownerEmail);
        fileService.revokeAccess(fileId, ownerEmail);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Access revoked. User notified by email."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/share-file
    // Owner shares file — OTP sent to recipient email
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/share-file")
    public ResponseEntity<FileDTO.ApiResponse> shareFile(
            @RequestBody FileDTO.ShareFileRequest request,
            @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /share-file fileId={} sharedWith={}", request.getFileId(), request.getSharedWithEmail());
        fileService.shareFile(request.getFileId(), request.getSharedWithEmail(), ownerEmail);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "File shared. OTP sent to " + request.getSharedWithEmail()));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/verify-and-download/{fileId}
    // User provides OTP — file is decrypted and returned as download
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/verify-and-download/{fileId}")
    public ResponseEntity<byte[]> verifyAndDownload(
            @PathVariable Long fileId,
            @RequestBody FileDTO.VerifyOtpRequest request,
            @RequestParam("ownerEmail") String ownerEmail) throws Exception {
 
        log.info("[CONTROLLER] POST /verify-and-download/{} requester={}", fileId, request.getRequesterEmail());
 
        byte[] fileBytes = fileService.verifyAndDownload(fileId, request.getOtp(),
                request.getRequesterEmail(), ownerEmail);
        UserFile raw = fileService.getRawFile(fileId);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + raw.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/audit-logs/{userId}
    // Owner sees all actions on their files
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/audit-logs/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getAuditLogs(@PathVariable Long userId) {
        log.info("[CONTROLLER] GET /audit-logs/{}", userId);
        List<FileDTO.AuditLogResponse> logs = fileService.getAuditLogs(userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Audit logs fetched.", logs));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/shared-with-me/{email}
    // User sees all files shared with their email
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/shared-with-me/{email}")
    public ResponseEntity<FileDTO.ApiResponse> getSharedWithMe(@PathVariable String email) {
        log.info("[CONTROLLER] GET /shared-with-me/{}", email);
        List<FileDTO.FileResponse> files = fileService.getSharedWithMe(email);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Shared files fetched.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/files/{fileId}
    // Owner deletes file from both Supabase and DB
    // ─────────────────────────────────────────────────────────────────
    @DeleteMapping("/{fileId}")
    public ResponseEntity<FileDTO.ApiResponse> deleteFile(
            @PathVariable Long fileId,
            @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] DELETE /files/{} owner={}", fileId, ownerEmail);
        fileService.deleteFile(fileId, ownerEmail);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "File deleted successfully."));
    }
}