package com.DataPro.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_files")
public class UserFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileHash;        // SHA-256 of decrypted content (for dedup)
    private Long userId;
    private String contentType;
    private String supabasePath;    // path inside Supabase bucket
    private String supabaseUrl;     // public/signed URL

    private boolean isPublic;       // true = community file

    private String shareOtp;
    private String sharedWithEmail;
    private LocalDateTime otpCreatedAt;

    private boolean accessRevoked = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getSupabasePath() { return supabasePath; }
    public void setSupabasePath(String supabasePath) { this.supabasePath = supabasePath; }

    public String getSupabaseUrl() { return supabaseUrl; }
    public void setSupabaseUrl(String supabaseUrl) { this.supabaseUrl = supabaseUrl; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getShareOtp() { return shareOtp; }
    public void setShareOtp(String shareOtp) { this.shareOtp = shareOtp; }

    public String getSharedWithEmail() { return sharedWithEmail; }
    public void setSharedWithEmail(String sharedWithEmail) { this.sharedWithEmail = sharedWithEmail; }

    public LocalDateTime getOtpCreatedAt() { return otpCreatedAt; }
    public void setOtpCreatedAt(LocalDateTime otpCreatedAt) { this.otpCreatedAt = otpCreatedAt; }

    public boolean isAccessRevoked() { return accessRevoked; }
    public void setAccessRevoked(boolean accessRevoked) { this.accessRevoked = accessRevoked; }
}