package com.eduapp.security;

import com.eduapp.model.User;
import com.eduapp.repository.UserRepository;
import com.eduapp.service.RoleNormalizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.getEmailFromToken(token);
                
                if (email != null && !email.isEmpty()) {
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        // Ensure user is active (default to true if null)
                        Boolean isActive = user.getIsActive();
                        if (isActive == null) {
                            isActive = true;
                            user.setIsActive(true);
                            userRepository.save(user);
                        }
                        
                        if (Boolean.TRUE.equals(isActive)) {
                            // Get and normalize role (default to STUDENT if null)
                            String roleName = RoleNormalizer.normalizeName(user.getRole());
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            List.of(new SimpleGrantedAuthority("ROLE_" + roleName))
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JWT authentication failed: " + e.getMessage());
            // Continue filter chain on error - let other handlers deal with it
        }

        filterChain.doFilter(request, response);
    }
}
