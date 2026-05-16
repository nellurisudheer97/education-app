package com.eduapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning user credentials during development/debugging
 * ⚠️ WARNING: This is for development only - DO NOT expose in production!
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialsDTO {
    private String email;
    private String plainPassword;
    private String fullName;
    private String role;
}
