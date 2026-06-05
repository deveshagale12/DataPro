 package com.DataPro.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import or
g.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.DataPro.dto.FileDTO;
import com.DataPro.entity.AccessRequest;
import com.DataPro.service.FileService;

import java.util.List;
import java.util.Map;

 
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
    // Upload any file: encrypted with AES-256, SHA-256 dedup check,
    // stored in Supabase, email sent on success
    // ─────────────────────────────────────────────────────────────────
    @PostMapping(value = "/upload-secure", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDTO.ApiResponse> uploadSecure(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam("uploaderEmail") String uploaderEmail,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic) throws Exception {
 
        log.info("[CONTROLLER] POST /upload-secure userId={} file={} isPublic={}", userId, file.getOriginalFilename(), isPublic);
 
        FileDTO.FileResponse response = fileService.uploadSecure(file, userId, uploaderEmail, isPublic);
 
        log.info("[CONTROLLER] Upload success. fileId={}", response.getId());
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
 
        log.info("[CONTROLLER] Returning {} files for userId={}", files.size(), userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Files fetched successfully.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/community-files/{userId}
    // Get all public/community files (any user can see)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/community-files/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getCommunityFiles(@PathVariable Long userId) {
 
        log.info("[CONTROLLER] GET /community-files/{}", userId);
 
        List<FileDTO.FileResponse> files = fileService.getCommunityFiles(userId);
 
        log.info("[CONTROLLER] Returning {} community files for userId={}", files.size(), userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Community files fetched successfully.", files));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/otp/send
    // Owner sends OTP to a specific email to grant file access
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/otp/send")
    public ResponseEntity<FileDTO.ApiResponse> sendOtp(@RequestBody FileDTO.OtpRequest request,
                                                        @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /otp/send fileId={} targetEmail={}", request.getFileId(), request.getTargetEmail());
 
        fileService.sendOtp(request.getFileId(), request.getTargetEmail(), ownerEmail);
 
        log.info("[CONTROLLER] OTP sent successfully for fileId={}", request.getFileId());
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "OTP sent to " + request.getTargetEmail() + " successfully."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/request-access
    // Any user requests access to a community/public file
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/request-access")
    public ResponseEntity<FileDTO.ApiResponse> requestAccess(@RequestBody FileDTO.AccessRequestDTO request,
                                                              @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /request-access fileId={} requester={}", request.getFileId(), request.getRequesterEmail());
 
        fileService.requestAccess(request.getFileId(), request.getRequesterEmail(), ownerEmail);
 
        log.info("[CONTROLLER] Access request saved for fileId={}", request.getFileId());
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Access request submitted. Owner has been notified."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/pending-requests/{userId}
    // Owner sees all pending access requests for their files
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/pending-requests/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getPendingRequests(@PathVariable Long userId) {
 
        log.info("[CONTROLLER] GET /pending-requests/{}", userId);
 
        List<AccessRequest> pending = fileService.getPendingRequests(userId);
 
        log.info("[CONTROLLER] Found {} pending requests for userId={}", pending.size(), userId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Pending requests fetched.", pending));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // PUT /api/files/revoke-access/{fileId}
    // Owner revokes access to a file and notifies the user by email
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/revoke-access/{fileId}")
    public ResponseEntity<FileDTO.ApiResponse> revokeAccess(@PathVariable Long fileId,
                                                             @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] PUT /revoke-access/{} ownerEmail={}", fileId, ownerEmail);
 
        fileService.revokeAccess(fileId, ownerEmail);
 
        log.info("[CONTROLLER] Access revoked for fileId={}", fileId);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Access revoked successfully. User notified by email."));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/share-file
    // Owner shares file with another user — OTP sent to their email
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/share-file")
    public ResponseEntity<FileDTO.ApiResponse> shareFile(@RequestBody FileDTO.ShareFileRequest request,
                                                          @RequestParam("ownerEmail") String ownerEmail) {
 
        log.info("[CONTROLLER] POST /share-file fileId={} sharedWith={}", request.getFileId(), request.getSharedWithEmail());
 
        fileService.shareFile(request.getFileId(), request.getSharedWithEmail(), ownerEmail);
 
        log.info("[CONTROLLER] File shared. OTP sent to {}", request.getSharedWithEmail());
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "File shared. OTP sent to " + request.getSharedWithEmail()));
    }
 
    // ─────────────────────────────────────────────────────────────────
    // POST /api/files/verify-and-download/{fileId}
    // Any user provides OTP → file is decrypted and returned as download
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/verify-and-download/{fileId}")
    public ResponseEntity<byte[]> verifyAndDownload(@PathVariable Long fileId,
                                                     @RequestBody FileDTO.VerifyOtpRequest request,
                                                     @RequestParam("ownerEmail") String ownerEmail) throws Exception {
 
        log.info("[CONTROLLER] POST /verify-and-download/{} requester={}", fileId, request.getRequesterEmail());
 
        byte[] fileBytes = fileService.verifyAndDownload(fileId, request.getOtp(),
                request.getRequesterEmail(), ownerEmail);
 
        log.info("[CONTROLLER] File decrypted and sent to requester={}", request.getRequesterEmail());
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + fileId + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileBytes);
    }
 
    // ─────────────────────────────────────────────────────────────────
    // GET /api/files/audit-logs/{userId}
    // Owner sees all actions (uploads, downloads, OTPs, revokes) on their files
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/audit-logs/{userId}")
    public ResponseEntity<FileDTO.ApiResponse> getAuditLogs(@PathVariable Long userId) {
 
        log.info("[CONTROLLER] GET /audit-logs/{}", userId);
 
        List<FileDTO.AuditLogResponse> logs = fileService.getAuditLogs(userId);
 
        log.info("[CONTROLLER] Returning {} audit log entries for userId={}", logs.size(), userId);
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
 
        log.info("[CONTROLLER] Found {} files shared with {}", files.size(), email);
        return ResponseEntity.ok(new FileDTO.ApiResponse(true, "Shared files fetched.", files));
    }
}
 