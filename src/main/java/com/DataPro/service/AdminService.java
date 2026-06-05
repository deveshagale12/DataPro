package com.DataPro.service;

import com.DataPro.entity.AccessRequest;
import com.DataPro.entity.DownloadLog;
import com.DataPro.entity.User;
import com.DataPro.entity.UserFile;
import com.DataPro.repository.AccessRequestRepository;
import com.DataPro.repository.DownloadLogRepository;
import com.DataPro.repository.UserFileRepository;
import com.DataPro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final EmailService emailService;

    public AdminService(
            UserRepository userRepository,
            UserFileRepository userFileRepository,
            DownloadLogRepository downloadLogRepository,
            AccessRequestRepository accessRequestRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
        this.downloadLogRepository = downloadLogRepository;
        this.accessRequestRepository = accessRequestRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalFiles", userFileRepository.count());
        stats.put("totalLogs", downloadLogRepository.count());
        stats.put("totalRequests", accessRequestRepository.count());

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Map<String, Object>> getAllFiles() {
        List<UserFile> files = userFileRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (UserFile file : files) {
            User owner = userRepository.findById(file.getUserId()).orElse(null);

            Map<String, Object> map = new HashMap<>();
            map.put("id", file.getId());
            map.put("fileName", file.getFileName());
            map.put("fileType", file.getContentType());
            map.put("ownerId", file.getUserId());
            map.put("ownerName", owner != null ? owner.getName() : "Unknown");
            map.put("ownerEmail", owner != null ? owner.getEmail() : "Unknown");
            map.put("isPublic", file.isPublic());
            map.put("supabaseUrl", file.getSupabaseUrl());
            map.put("otpCreatedAt", file.getOtpCreatedAt());
            map.put("accessRevoked", file.isAccessRevoked());

            response.add(map);
        }

        return response;
    }

    public Map<String, Long> getUploadStats() {
        List<UserFile> files = userFileRepository.findAll();

        return files.stream()
                .filter(file -> file.getOtpCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        file -> file.getOtpCreatedAt().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getRoleDistribution() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getRole() != null ? user.getRole() : "UNKNOWN",
                        Collectors.counting()
                ));
    }

    public List<DownloadLog> getGlobalLogs() {
        return downloadLogRepository.findAll();
    }

    public List<AccessRequest> getAllAccessRequests() {
        return accessRequestRepository.findAll();
    }

    public Map<String, Object> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        String email = user.getEmail();
        String name = user.getName();

        List<UserFile> files = userFileRepository.findByUserId(id);
        for (UserFile file : files) {
            userFileRepository.delete(file);
        }

        List<AccessRequest> requests = accessRequestRepository.findAll();
        for (AccessRequest request : requests) {
            if (Objects.equals(request.getOwnerId(), id)) {
                accessRequestRepository.delete(request);
            }
        }

        List<DownloadLog> logs = downloadLogRepository.findByOwnerIdOrderByDownloadTimeDesc(id);
        for (DownloadLog log : logs) {
            downloadLogRepository.delete(log);
        }

        userRepository.deleteById(id);

        emailService.sendUserDeletedEmail(email, name);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User, files, requests and logs deleted successfully.");
        response.put("deletedUserId", id);

        return response;
    }
}