package com.eduapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        // Use UTF-8 encoding directly for maximum compatibility
        // This works with any string secret without requiring Base64 encoding
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        // Ensure minimum key size of 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            // If secret is too short, we need to extend it
            // This is for development only - use a longer secret in production
            byte[] extendedKey = new byte[32];
            System.arraycopy(keyBytes, 0, extendedKey, 0, keyBytes.length);
            // Pad remaining bytes if needed
            for (int i = keyBytes.length; i < 32; i++) {
                extendedKey[i] = (byte) (keyBytes[i % keyBytes.length] + i);
            }
            keyBytes = extendedKey;
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
