package com.eduapp.controller;

import com.eduapp.dto.UserCredentialsDTO;
import com.eduapp.model.User;
import com.eduapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * Developer-only endpoint for retrieving user credentials during development/debugging
 * ⚠️ WARNING: This should ONLY be exposed in development environments!
 * Remove this controller before deploying to production!
 */
@RestController
@RequestMapping("/dev/credentials")
public class DeveloperCredentialsController {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get user credentials by email (Development only)
     * ⚠️ Only accessible to DEVELOPER role
     */
    @GetMapping("/{email}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<?> getUserCredentials(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email.toLowerCase().trim());
        
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found: " + email);
        }
        
        User foundUser = user.get();
        UserCredentialsDTO credentials = new UserCredentialsDTO(
            foundUser.getEmail(),
            foundUser.getPlainPassword(),
            foundUser.getFullName(),
            foundUser.getRole().toString()
        );
        
        return ResponseEntity.ok(credentials);
    }
    
    /**
     * Get all user credentials (Development only)
     * ⚠️ Only accessible to DEVELOPER role - Returns sensitive data!
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<?> getAllCredentials() {
        return ResponseEntity.ok(
            userRepository.findAll().stream()
                .map(user -> new UserCredentialsDTO(
                    user.getEmail(),
                    user.getPlainPassword(),
                    user.getFullName(),
                    user.getRole().toString()
                ))
                .toList()
        );
    }
}
