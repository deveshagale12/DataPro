package com.DataPro.exception;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
 
    // ── Duplicate File ────────────────────────────────────────────────
    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateFile(DuplicateFileException ex) {
        log.warn("Duplicate file detected: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, "DUPLICATE_FILE", ex.getMessage());
    }
 
    // ── File Not Found ────────────────────────────────────────────────
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException ex) {
        log.warn("File not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", ex.getMessage());
    }
 
    // ── Invalid OTP ───────────────────────────────────────────────────
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOtp(InvalidOtpException ex) {
        log.warn("Invalid OTP attempt: {}", ex.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, "INVALID_OTP", ex.getMessage());
    }
 
    // ── Access Denied ─────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildError(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }
 
    // ── Encryption Error ──────────────────────────────────────────────
    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<Map<String, Object>> handleEncryption(EncryptionException ex) {
        log.error("Encryption/Decryption error: {}", ex.getMessage());
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "ENCRYPTION_ERROR", ex.getMessage());
    }
 
    // ── Max Upload Size ───────────────────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException ex) {
        log.warn("File size exceeded: {}", ex.getMessage());
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "File size exceeds the maximum allowed limit.");
    }
 
    // ── Supabase Error ────────────────────────────────────────────────
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String, Object>> handleStorage(StorageException ex) {
        log.error("Supabase storage error: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_GATEWAY, "STORAGE_ERROR", ex.getMessage());
    }
 
    // ── Generic Fallback ──────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.");
    }
 
    // ── Helper ────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("errorCode", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }
}
 
 