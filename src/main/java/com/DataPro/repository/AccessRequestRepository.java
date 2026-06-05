package com.DataPro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DataPro.entity.AccessRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByOwnerIdAndStatus(Long ownerId, String status);
    Optional<AccessRequest> findByFileIdAndRequesterEmail(Long fileId, String requesterEmail);
    List<AccessRequest> findByRequesterEmail(String requesterEmail);
}