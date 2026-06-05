package com.DataPro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DataPro.entity.DownloadLog;

import java.util.List;

@Repository
public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
    List<DownloadLog> findByOwnerIdOrderByDownloadTimeDesc(Long ownerId);
    List<DownloadLog> findByFileId(Long fileId);
}