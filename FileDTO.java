package com.DataPro.dto;


import java.time.LocalDateTime;

public class FileDTO {

    // ─────────────────── API Response ────────────────────
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    // ─────────────────── File Info Response ──────────────
    public static class FileResponse {
        private Long id;
        private String fileName;
        private String contentType;
        private Long userId;
        private boolean isPublic;
        private String supabaseUrl;
        private LocalDateTime otpCreatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
        public String getSupabaseUrl() { return supabaseUrl; }
        public void setSupabaseUrl(String supabaseUrl) { this.supabaseUrl = supabaseUrl; }
        public LocalDateTime getOtpCreatedAt() { return otpCreatedAt; }
        public void setOtpCreatedAt(LocalDateTime otpCreatedAt) { this.otpCreatedAt = otpCreatedAt; }
    }

    // ─────────────────── OTP Send Request ────────────────
    public static class OtpRequest {
        private Long fileId;
        private String targetEmail;

        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public String getTargetEmail() { return targetEmail; }
        public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
    }

    // ─────────────────── Share File Request ──────────────
    public static class ShareFileRequest {
        private Long fileId;
        private String sharedWithEmail;

        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public String getSharedWithEmail() { return sharedWithEmail; }
        public void setSharedWithEmail(String email) { this.sharedWithEmail = email; }
    }

    // ─────────────────── Access Request ──────────────────
    public static class AccessRequestDTO {
        private Long fileId;
        private String requesterEmail;

        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public String getRequesterEmail() { return requesterEmail; }
        public void setRequesterEmail(String email) { this.requesterEmail = email; }
    }

    // ─────────────────── Verify OTP + Download ───────────
    public static class VerifyOtpRequest {
        private String otp;
        private String requesterEmail;

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        public String getRequesterEmail() { return requesterEmail; }
        public void setRequesterEmail(String email) { this.requesterEmail = email; }
    }

    // ─────────────────── Audit Log Response ──────────────
    public static class AuditLogResponse {
        private Long id;
        private Long fileId;
        private String fileName;
        private String downloaderEmail;
        private String action;
        private LocalDateTime downloadTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFileId() { return fileId; }
        public void setFileId(Long fileId) { this.fileId = fileId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getDownloaderEmail() { return downloaderEmail; }
        public void setDownloaderEmail(String email) { this.downloaderEmail = email; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public LocalDateTime getDownloadTime() { return downloadTime; }
        public void setDownloadTime(LocalDateTime time) { this.downloadTime = time; }
    }
}