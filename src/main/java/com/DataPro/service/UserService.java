package com.DataPro.service;

import com.DataPro.dto.UserDTO;
import com.DataPro.entity.User;
import com.DataPro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
 
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
 
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
 
    private final UserRepository userRepository;
    private final EmailService emailService;
 
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
 
    // ── REGISTER ──────────────────────────────────────────────────────
    public UserDTO.ApiResponse register(UserDTO.RegisterRequest req) {
        log.info("[REGISTER] email={}", req.getEmail());
        if (userRepository.existsByEmail(req.getEmail())) {
            log.warn("[REGISTER] Email already exists: {}", req.getEmail());
            return new UserDTO.ApiResponse(false, "Email already registered.");
        }
 
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setMobile(req.getMobile());
        user.setAddress(req.getAddress());
        user.setEducation(req.getEducation());
        user.setDob(req.getDob());
        user.setProfilePic(req.getProfilePic());
        user.setRole(req.getRole() != null ? req.getRole().toUpperCase() : "USER");
 
        userRepository.save(user);
        log.info("[REGISTER] User created id={} email={}", user.getId(), user.getEmail());
 
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        return new UserDTO.ApiResponse(true, "User registered successfully.", toResponse(user));
    }
 
    // ── LOGIN ─────────────────────────────────────────────────────────
    public UserDTO.ApiResponse login(UserDTO.LoginRequest req) {
        log.info("[LOGIN] email={}", req.getEmail());
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            log.warn("[LOGIN] Email not found: {}", req.getEmail());
            return new UserDTO.ApiResponse(false, "Invalid email or password.");
        }
 
        User user = optional.get();
        if (!user.getPassword().equals(req.getPassword())) {
            log.warn("[LOGIN] Wrong password for email={}", req.getEmail());
            return new UserDTO.ApiResponse(false, "Invalid email or password.");
        }
 
        log.info("[LOGIN] Success userId={}", user.getId());
        return new UserDTO.ApiResponse(true, "Login successful.", toResponse(user));
    }
 
    // ── FIND BY EMAIL ─────────────────────────────────────────────────
    public UserDTO.ApiResponse findByEmail(String email) {
        log.info("[FIND-BY-EMAIL] email={}", email);
        return userRepository.findByEmail(email)
                .map(user -> {
                    log.info("[FIND-BY-EMAIL] Found userId={}", user.getId());
                    return new UserDTO.ApiResponse(true, "User found.", toResponse(user));
                })
                .orElseGet(() -> {
                    log.warn("[FIND-BY-EMAIL] Not found email={}", email);
                    return new UserDTO.ApiResponse(false, "User not found with email: " + email);
                });
    }
 
    // ── GET ALL USERS ─────────────────────────────────────────────────
    public UserDTO.ApiResponse getAllUsers() {
        log.info("[GET-ALL-USERS] Fetching all users");
        List<User> users = userRepository.findAll();
        List<UserDTO.UserResponse> responses = users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        log.info("[GET-ALL-USERS] Total users found={}", users.size());
        return new UserDTO.ApiResponse(true, "All users fetched successfully.", responses);
    }
 
    // ── GET USER BY ID ────────────────────────────────────────────────
    public UserDTO.ApiResponse getUserById(Long id) {
        log.info("[GET-USER-BY-ID] id={}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("[GET-USER-BY-ID] Found email={}", user.getEmail());
                    return new UserDTO.ApiResponse(true, "User found.", toResponse(user));
                })
                .orElseGet(() -> {
                    log.warn("[GET-USER-BY-ID] Not found id={}", id);
                    return new UserDTO.ApiResponse(false, "User not found with id: " + id);
                });
    }
 
    // ── UPDATE USER ───────────────────────────────────────────────────
    public UserDTO.ApiResponse updateUser(Long id, UserDTO.UpdateRequest req) {
        log.info("[UPDATE-USER] id={}", id);
        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            log.warn("[UPDATE-USER] Not found id={}", id);
            return new UserDTO.ApiResponse(false, "User not found with id: " + id);
        }
 
        User user = optional.get();
        if (req.getName()      != null) user.setName(req.getName());
        if (req.getMobile()    != null) user.setMobile(req.getMobile());
        if (req.getAddress()   != null) user.setAddress(req.getAddress());
        if (req.getEducation() != null) user.setEducation(req.getEducation());
        if (req.getDob()       != null) user.setDob(req.getDob());
        if (req.getProfilePic()!= null) user.setProfilePic(req.getProfilePic());
 
        userRepository.save(user);
        log.info("[UPDATE-USER] Updated userId={}", user.getId());
        return new UserDTO.ApiResponse(true, "User updated successfully.", toResponse(user));
    }
 
    // ── DELETE USER ───────────────────────────────────────────────────
    public UserDTO.ApiResponse deleteUser(Long id) {
        log.info("[DELETE-USER] id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("[DELETE-USER] Not found id={}", id);
            return new UserDTO.ApiResponse(false, "User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("[DELETE-USER] Deleted userId={}", id);
        return new UserDTO.ApiResponse(true, "User deleted successfully.");
    }
 
    // ── FORGOT PASSWORD ───────────────────────────────────────────────
    public UserDTO.ApiResponse forgotPassword(UserDTO.ForgotPasswordRequest req) {
        log.info("[FORGOT-PASSWORD] email={}", req.getEmail());
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            log.warn("[FORGOT-PASSWORD] Email not found: {}", req.getEmail());
            return new UserDTO.ApiResponse(false, "No account found with this email.");
        }
 
        User user = optional.get();
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        log.info("[FORGOT-PASSWORD] OTP generated and saved for userId={}", user.getId());
 
        emailService.sendOtpEmail(user.getEmail(), otp);
        return new UserDTO.ApiResponse(true, "OTP sent to your registered email address.");
    }
 
    // ── RESET PASSWORD ────────────────────────────────────────────────
    public UserDTO.ApiResponse resetPassword(UserDTO.ResetPasswordRequest req) {
        log.info("[RESET-PASSWORD] email={}", req.getEmail());
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            return new UserDTO.ApiResponse(false, "No account found with this email.");
        }
 
        User user = optional.get();
 
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return new UserDTO.ApiResponse(false, "No OTP was requested. Please request a new OTP.");
        }
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            log.warn("[RESET-PASSWORD] OTP expired for userId={}", user.getId());
            return new UserDTO.ApiResponse(false, "OTP has expired. Please request a new one.");
        }
        if (!user.getOtp().equals(req.getOtp())) {
            log.warn("[RESET-PASSWORD] Invalid OTP for userId={}", user.getId());
            return new UserDTO.ApiResponse(false, "Invalid OTP.");
        }
 
        user.setPassword(req.getNewPassword());
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        log.info("[RESET-PASSWORD] Password reset success userId={}", user.getId());
        return new UserDTO.ApiResponse(true, "Password reset successfully.");
    }
 
    // ── HELPERS ───────────────────────────────────────────────────────
    private String generateOtp() {
        return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }
 
    private UserDTO.UserResponse toResponse(User user) {
        UserDTO.UserResponse res = new UserDTO.UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setMobile(user.getMobile());
        res.setAddress(user.getAddress());
        res.setEducation(user.getEducation());
        res.setDob(user.getDob());
        res.setProfilePic(user.getProfilePic());
        return res;
    }
}
 