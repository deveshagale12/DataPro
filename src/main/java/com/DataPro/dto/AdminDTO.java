public class AdminDTO {

    public static class UserAdminResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String mobile;
        private String address;
        private String education;
        private LocalDate dob;

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
    }

    public static class FileAdminResponse {
        private Long id;
        private String fileName;
        private String fileType;
        private Long ownerId;
        private String ownerName;
        private String ownerEmail;
        private boolean isPublic;
        private String supabaseUrl;
        private LocalDateTime otpCreatedAt;
        private boolean accessRevoked;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

        public String getOwnerEmail() { return ownerEmail; }
        public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean aPublic) { isPublic = aPublic; }

        public String getSupabaseUrl() { return supabaseUrl; }
        public void setSupabaseUrl(String supabaseUrl) { this.supabaseUrl = supabaseUrl; }

        public LocalDateTime getOtpCreatedAt() { return otpCreatedAt; }
        public void setOtpCreatedAt(LocalDateTime otpCreatedAt) { this.otpCreatedAt = otpCreatedAt; }

        public boolean isAccessRevoked() { return accessRevoked; }
        public void setAccessRevoked(boolean accessRevoked) { this.accessRevoked = accessRevoked; }
    }
}