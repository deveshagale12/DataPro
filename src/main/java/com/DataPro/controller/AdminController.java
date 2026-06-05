package com.DataPro.controller;

import com.DataPro.entity.DownloadLog;
import com.DataPro.entity.User;
import com.DataPro.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")

@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/all-files")
    public ResponseEntity<List<Map<String, Object>>> getAllFiles() {
        return ResponseEntity.ok(adminService.getAllFiles());
    }

    @GetMapping("/upload-stats")
    public ResponseEntity<Map<String, Long>> getUploadStats() {
        return ResponseEntity.ok(adminService.getUploadStats());
    }

    @GetMapping("/role-distribution")
    public ResponseEntity<Map<String, Long>> getRoleDistribution() {
        return ResponseEntity.ok(adminService.getRoleDistribution());
    }

    @GetMapping("/global-logs")
    public ResponseEntity<List<DownloadLog>> getGlobalLogs() {
        return ResponseEntity.ok(adminService.getGlobalLogs());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }
}