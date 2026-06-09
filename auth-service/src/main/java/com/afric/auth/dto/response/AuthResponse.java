package com.afric.auth.dto.response;

import java.time.Instant;

/**
 * DTO retourné après inscription ou connexion réussie.
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
