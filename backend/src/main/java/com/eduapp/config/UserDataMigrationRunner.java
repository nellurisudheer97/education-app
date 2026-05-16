package com.eduapp.config;

import com.eduapp.model.User;
import com.eduapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class UserDataMigrationRunner {

    @Bean
    CommandLineRunner activateInactiveUsers(UserRepository userRepository) {
        return args -> {
            try {
                // Find all users with NULL or false isActive status
                List<User> inactiveUsers = userRepository.findAll().stream()
                    .filter(user -> user.getIsActive() == null || !user.getIsActive())
                    .toList();
                
                if (inactiveUsers.isEmpty()) {
                    System.out.println("✓ All users are already active");
                    return;
                }

                // Activate all inactive users
                inactiveUsers.forEach(user -> user.setIsActive(true));
                userRepository.saveAll(inactiveUsers);
                
                System.out.println("✓ Activated " + inactiveUsers.size() + " inactive user(s)");
            } catch (Exception e) {
                System.err.println("⚠ User activation migration warning: " + e.getMessage());
                // Don't fail startup if migration encounters issues
            }
        };
    }
}
