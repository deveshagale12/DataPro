package com.DataPro.service;

import com.DataPro.dto.UserDTO;
import com.DataPro.entity.User;
import com.DataPro.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ─────────────────────────── REGISTER ────────────────────────────

    public UserDTO.ApiResponse register(UserDTO.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
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

        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

        return new UserDTO.ApiResponse(true, "User registered successfully.", toResponse(user));
    }

    // ─────────────────────────── LOGIN ───────────────────────────────

    public UserDTO.ApiResponse login(UserDTO.LoginRequest req) {
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            return new UserDTO.ApiResponse(false, "Invalid email or password.");
        }

        User user = optional.get();
        if (!user.getPassword().equals(req.getPassword())) {
            return new UserDTO.ApiResponse(false, "Invalid email or password.");
        }

        return new UserDTO.ApiResponse(true, "Login successful.", toResponse(user));
    }

    // ─────────────────────────── FIND BY EMAIL ───────────────────────

    public UserDTO.ApiResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserDTO.ApiResponse(true, "User found.", toResponse(user)))
                .orElse(new UserDTO.ApiResponse(false, "User not found with email: " + email));
    }

    // ─────────────────────────── FORGOT PASSWORD ─────────────────────

    public UserDTO.ApiResponse forgotPassword(UserDTO.ForgotPasswordRequest req) {
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            return new UserDTO.ApiResponse(false, "No account found with this email.");
        }

        User user = optional.get();
        String otp = generateOtp();

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return new UserDTO.ApiResponse(true, "OTP sent to your registered email address.");
    }

    // ─────────────────────────── RESET PASSWORD ──────────────────────

    public UserDTO.ApiResponse resetPassword(UserDTO.ResetPasswordRequest req) {
        Optional<User> optional = userRepository.findByEmail(req.getEmail());
        if (optional.isEmpty()) {
            return new UserDTO.ApiResponse(false, "No account found with this email.");
        }

        User user = optional.get();

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            return new UserDTO.ApiResponse(false, "No OTP was requested. Please request a new OTP.");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return new UserDTO.ApiResponse(false, "OTP has expired. Please request a new one.");
        }

        if (!user.getOtp().equals(req.getOtp())) {
            return new UserDTO.ApiResponse(false, "Invalid OTP.");
        }

        user.setPassword(req.getNewPassword());
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return new UserDTO.ApiResponse(true, "Password reset successfully.");
    }

    // ─────────────────────────── HELPERS ─────────────────────────────

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
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