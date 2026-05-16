package com.eduapp.service;

import com.eduapp.model.UserRole;

public final class RoleNormalizer {
    private RoleNormalizer() {}

    public static UserRole normalize(UserRole role) {
        if (role == null) return UserRole.STUDENT;
        return role == UserRole.DEVELOPER ? UserRole.INSTRUCTOR : role;
    }

    public static String normalizeName(UserRole role) {
        return normalize(role).name();
    }
}
