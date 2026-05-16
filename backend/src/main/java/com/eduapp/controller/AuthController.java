package com.eduapp.controller;

import com.eduapp.dto.AuthResponse;
import com.eduapp.dto.ForgotPasswordRequest;
import com.eduapp.dto.LoginRequest;
import com.eduapp.dto.RegisterRequest;
import com.eduapp.dto.ResetPasswordRequest;
import com.eduapp.dto.VerifyResetTokenRequest;
import com.eduapp.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Server is running");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        AuthResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<AuthResponse> verifyResetToken(
            @RequestBody VerifyResetTokenRequest request) {
        AuthResponse response = authService.verifyResetToken(request);
        if (response.getToken() != null || "Token is valid".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        AuthResponse response = authService.resetPassword(request);
        if ("Password reset successful. Please login with your new password".equals(response.getMessage())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
