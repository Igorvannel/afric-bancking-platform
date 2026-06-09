package com.afric.auth.dto.response;

import com.afric.auth.document.User;

import java.time.Instant;

/**
 * DTO utilisateur exposé au frontend (sans mot de passe).
 */
public record UserResponse(
    String id,
    String name,
    String email,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
