package com.DataPro.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailTaskExecutor")
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP - DataPro Password Reset");
            message.setText(
                "Hello,\n\n" +
                "Your One-Time Password (OTP) for resetting your DataPro account password is:\n\n" +
                "  OTP: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to DataPro!");
            message.setText(
                "Hi " + name + ",\n\n" +
                "Welcome to DataPro! Your account has been created successfully.\n\n" +
                "You can now log in using your registered email and password.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }
    
    // ── Upload success notification ──────────────────────────────────
    @Async("emailTaskExecutor")
    public void sendUploadSuccessEmail(String toEmail, String fileName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("File Uploaded Successfully - DataPro");
            msg.setText(
                "Hello,\n\n" +
                "Your file \"" + fileName + "\" has been uploaded and stored securely.\n" +
                "It has been encrypted with AES-256 and deduplicated using SHA-256 fingerprinting.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(msg);
            log.info("Upload success email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send upload email to {}: {}", toEmail, e.getMessage());
        }
    }
 
    // ── OTP share email ──────────────────────────────────────────────
    @Async("emailTaskExecutor")
    public void sendOtpEmail(String toEmail, String otp, String fileName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("File Access OTP - DataPro");
            msg.setText(
                "Hello,\n\n" +
                "You have been granted access to download the file: \"" + fileName + "\"\n\n" +
                "Your One-Time Password (OTP) is:  " + otp + "\n\n" +
                "This OTP is valid for 10 minutes. Use it at /api/files/verify-and-download/{fileId}\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(msg);
            log.info("OTP email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
        }
    }
 
 @@Async("emailTaskExecutor")
public void sendUserDeletedEmail(String toEmail, String name) {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("DataPro Account Removed");
        message.setText(
                "Hello " + name + ",\n\n" +
                "Your DataPro account has been removed by the administrator.\n\n" +
                "If you think this was a mistake, please contact support.\n\n" +
                "Regards,\nDataPro Team"
        );

        mailSender.send(message);

    } catch (Exception e) {
        System.out.println("Failed to send delete user email: " + e.getMessage());
    }
}
    // ── Access request notification to owner ────────────────────────
    @Async("emailTaskExecutor")
    public void sendAccessRequestEmail(String ownerEmail, String requesterEmail, String fileName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(ownerEmail);
            msg.setSubject("File Access Request - DataPro");
            msg.setText(
                "Hello,\n\n" +
                requesterEmail + " has requested access to your file: \"" + fileName + "\"\n\n" +
                "Please log in to approve or reject this request.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(msg);
            log.info("Access request email sent to owner {}", ownerEmail);
        } catch (Exception e) {
            log.error("Failed to send access request email: {}", e.getMessage());
        }
    }
 
    // ── Access revoked notification ──────────────────────────────────
    @Async("emailTaskExecutor")
    public void sendRevokeNotificationEmail(String toEmail, String fileName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(toEmail);
            msg.setSubject("File Access Revoked - DataPro");
            msg.setText(
                "Hello,\n\n" +
                "Your access to the file \"" + fileName + "\" has been revoked by the owner.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(msg);
            log.info("Revoke notification sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send revoke email: {}", e.getMessage());
        }
    }
 
    // ── Download notification to owner ───────────────────────────────
    @Async("emailTaskExecutor")
    public void sendDownloadNotificationEmail(String ownerEmail, String downloaderEmail, String fileName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(ownerEmail);
            msg.setSubject("Your File Was Downloaded - DataPro");
            msg.setText(
                "Hello,\n\n" +
                "Your file \"" + fileName + "\" was downloaded by: " + downloaderEmail + "\n\n" +
                "If this was unexpected, please revoke access immediately.\n\n" +
                "Regards,\nDataPro Team"
            );
            mailSender.send(msg);
            log.info("Download notification sent to owner {}", ownerEmail);
        } catch (Exception e) {
            log.error("Failed to send download notification: {}", e.getMessage());
        }
       }
}