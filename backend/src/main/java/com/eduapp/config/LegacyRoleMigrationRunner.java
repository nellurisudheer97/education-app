package com.eduapp.config;

import com.eduapp.model.User;
import com.eduapp.model.UserRole;
import com.eduapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LegacyRoleMigrationRunner {

    @Bean
    CommandLineRunner migrateDeveloperRole(UserRepository userRepository) {
        return args -> {
            try {
                List<User> legacyUsers = userRepository.findByRole(UserRole.DEVELOPER);
                if (legacyUsers.isEmpty()) return;

                legacyUsers.forEach(user -> user.setRole(UserRole.INSTRUCTOR));
                userRepository.saveAll(legacyUsers);
            } catch (Exception e) {
                System.err.println("Migration failed: " + e.getMessage());
            }
        };
    }
}
