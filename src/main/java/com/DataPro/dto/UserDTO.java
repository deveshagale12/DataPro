package com.DataPro.dto;

import java.time.LocalDate;


public class UserDTO {

    // ───────────────────────────── Register ──────────────────────────
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String mobile;
        private String address;
        private String education;
        private LocalDate dob;
        private String profilePic;
        private String role;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }

        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }

        public String getProfilePic() { return profilePic; }
        public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    // ───────────────────────────── Login ─────────────────────────────
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ───────────────────────────── Forgot Password ───────────────────
    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ───────────────────────────── Reset Password ────────────────────
    public static class ResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    // ───────────────────────────── API Response ───────────────────────
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    // ───────────────────────────── User Response ─────────────────────
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String mobile;
        private String address;
        private String education;
        private LocalDate dob;
        private String profilePic;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }

        public LocalDate getDob() { return dob; }
        public void setDob(LocalDate dob) { this.dob = dob; }

        public String getProfilePic() { return profilePic; }
        public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    }
}