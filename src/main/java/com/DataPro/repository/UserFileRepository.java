package com.DataPro.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DataPro.entity.UserFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserId(Long userId);
    List<UserFile> findByIsPublicTrue();
    Optional<UserFile> findByFileHash(String fileHash);
    boolean existsByFileHash(String fileHash);
    List<UserFile> findBySharedWithEmail(String email);
}