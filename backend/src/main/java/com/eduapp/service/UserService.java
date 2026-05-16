package com.eduapp.service;

import com.eduapp.dto.UserDTO;
import com.eduapp.model.User;
import com.eduapp.model.UserRole;
import com.eduapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getActiveStudents() {
        return userRepository.findByRoleAndIsActiveTrue(UserRole.STUDENT)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                RoleNormalizer.normalizeName(user.getRole())
        );
    }
}
