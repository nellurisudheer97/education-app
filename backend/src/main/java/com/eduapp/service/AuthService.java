package com.eduapp.service;

import com.eduapp.dto.AuthResponse;
import com.eduapp.dto.ForgotPasswordRequest;
import com.eduapp.dto.LoginRequest;
import com.eduapp.dto.RegisterRequest;
import com.eduapp.dto.ResetPasswordRequest;
import com.eduapp.dto.VerifyResetTokenRequest;
import com.eduapp.model.PasswordResetToken;
import com.eduapp.model.User;
import com.eduapp.model.UserRole;
import com.eduapp.repository.PasswordResetTokenRepository;
import com.eduapp.repository.UserRepository;
import com.eduapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private static final String SAMPLE_PASSWORD = "password123";
    private static final String LEGACY_SAMPLE_HASH = "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final long RESET_TOKEN_EXPIRY_HOURS = 24;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            return new AuthResponse(null, "Email already exists", null, null, null);
        }
        
        try {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            // Development only: Store plain password for debugging
            user.setPlainPassword(request.getPassword());
            user.setFullName(request.getFullName().trim());
            
            // Always set role to STUDENT for new registrations
            user.setRole(UserRole.STUDENT);
            
            // Always activate new users
            user.setIsActive(true);
            
            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(savedUser.getEmail());
            
            return new AuthResponse(
                token, 
                "Registration successful", 
                savedUser.getId(),
                toClientRole(savedUser),
                savedUser.getFullName()
            );
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, "Registration failed: " + e.getMessage(), null, null, null);
        }
    }
    
    public AuthResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElse(null);
            
            if (user == null) {
                return new AuthResponse(null, "Invalid email or password", null, null, null);
            }
            
            if (!passwordMatches(request.getPassword(), user)) {
                return new AuthResponse(null, "Invalid email or password", null, null, null);
            }
            
            // Ensure user is active (default to true if null)
            if (user.getIsActive() == null || !user.getIsActive()) {
                user.setIsActive(true);
            }
            
            // Ensure user has a role (default to STUDENT if null)
            if (user.getRole() == null) {
                user.setRole(UserRole.STUDENT);
            }
            
            // Save any changes
            user = userRepository.save(user);
            
            String token = jwtUtil.generateToken(user.getEmail());
            
            return new AuthResponse(
                token,
                "Login successful",
                user.getId(),
                toClientRole(user),
                user.getFullName()
            );
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, "Login failed: " + e.getMessage(), null, null, null);
        }
    }

    private String toClientRole(User user) {
        return RoleNormalizer.normalizeName(user.getRole());
    }

    private boolean passwordMatches(String rawPassword, User user) {
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return true;
        }

        if (LEGACY_SAMPLE_HASH.equals(user.getPassword()) && SAMPLE_PASSWORD.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
    
    // Password Reset Methods
    
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        try {
            String email = normalizeEmail(request.getEmail());
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                // Don't reveal if email exists for security reasons
                return new AuthResponse(null, "If an account with that email exists, a reset link has been sent", null, null, null);
            }
            
            // Invalidate any existing reset tokens
            passwordResetTokenRepository.findByUserIdAndUsedFalse(user.getId())
                .ifPresent(token -> {
                    token.setUsed(true);
                    passwordResetTokenRepository.save(token);
                });
            
            // Generate new reset token
            String resetToken = UUID.randomUUID().toString();
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(resetToken);
            token.setUser(user);
            token.setExpiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));
            token.setUsed(false);
            
            passwordResetTokenRepository.save(token);
            
            // Send email
            boolean emailSent = emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
            
            if (emailSent) {
                return new AuthResponse(null, "If an account with that email exists, a reset link has been sent", null, null, null);
            } else {
                return new AuthResponse(null, "Failed to send reset email. Please try again later", null, null, null);
            }
        } catch (Exception e) {
            System.err.println("Forgot password error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, "Password reset request failed. Please try again", null, null, null);
        }
    }
    
    public AuthResponse verifyResetToken(VerifyResetTokenRequest request) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken()).orElse(null);
            
            if (resetToken == null) {
                return new AuthResponse(null, "Invalid reset token", null, null, null);
            }
            
            if (!resetToken.isValid()) {
                return new AuthResponse(null, "Reset token has expired or already used", null, null, null);
            }
            
            return new AuthResponse(null, "Token is valid", null, null, null);
        } catch (Exception e) {
            System.err.println("Token verification error: " + e.getMessage());
            return new AuthResponse(null, "Token verification failed", null, null, null);
        }
    }
    
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        try {
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                return new AuthResponse(null, "New password is required", null, null, null);
            }
            
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return new AuthResponse(null, "Passwords do not match", null, null, null);
            }
            
            if (request.getNewPassword().length() < 6) {
                return new AuthResponse(null, "Password must be at least 6 characters", null, null, null);
            }
            
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken()).orElse(null);
            
            if (resetToken == null) {
                return new AuthResponse(null, "Invalid reset token", null, null, null);
            }
            
            if (!resetToken.isValid()) {
                return new AuthResponse(null, "Reset token has expired or already used", null, null, null);
            }
            
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            // Development only: Store plain password for debugging
            user.setPlainPassword(request.getNewPassword());
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            
            return new AuthResponse(null, "Password reset successful. Please login with your new password", null, null, null);
        } catch (Exception e) {
            System.err.println("Password reset error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResponse(null, "Password reset failed. Please try again", null, null, null);
        }
    }
}
