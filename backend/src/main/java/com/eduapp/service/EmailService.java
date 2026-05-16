package com.eduapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender javaMailSender;
    
    @Value("${spring.mail.username:noreply@eduapp.com}")
    private String fromEmail;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    public boolean sendPasswordResetEmail(String userEmail, String fullName, String resetToken) {
        try {
            // If mail sender is not configured, skip email sending (for development)
            if (javaMailSender == null) {
                System.out.println("⚠️  Email not configured. Reset link: " + frontendUrl + "/reset-password?token=" + resetToken);
                return true;
            }
            
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Password Reset Request - EduForge");
            message.setText(buildPasswordResetEmailBody(fullName, resetLink));
            
            javaMailSender.send(message);
            System.out.println("✓ Password reset email sent to " + userEmail);
            return true;
        } catch (Exception e) {
            System.err.println("✗ Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String buildPasswordResetEmailBody(String fullName, String resetLink) {
        return "Hello " + fullName + ",\n\n" +
               "We received a request to reset your password. Click the link below to create a new password:\n\n" +
               resetLink + "\n\n" +
               "This link will expire in 24 hours.\n\n" +
               "If you didn't request this, please ignore this email and your password will remain unchanged.\n\n" +
               "Best regards,\n" +
               "EduForge Team";
    }
}
