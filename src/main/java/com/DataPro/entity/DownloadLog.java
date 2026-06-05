package com.DataPro.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "download_logs")
public class DownloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private String fileName;
    private String downloaderEmail;
    private LocalDateTime downloadTime;
    private String action; // "DOWNLOAD", "ACCESS_REQUESTED", "ACCESS_REVOKED", "OTP_SENT"
    private Long ownerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDownloaderEmail() { return downloaderEmail; }
    public void setDownloaderEmail(String downloaderEmail) { this.downloaderEmail = downloaderEmail; }

    public LocalDateTime getDownloadTime() { return downloadTime; }
    public void setDownloadTime(LocalDateTime downloadTime) { this.downloadTime = downloadTime; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}