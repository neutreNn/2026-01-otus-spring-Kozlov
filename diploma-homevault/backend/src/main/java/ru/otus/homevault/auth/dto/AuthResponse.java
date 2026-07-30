package ru.otus.homevault.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.homevault.users.dto.UserResponse;

import java.time.Instant;

@Schema(description = "Authentication response")
public record AuthResponse(
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.signature")
        String accessToken,

        @Schema(description = "Access token expiration timestamp", example = "2026-07-18T10:30:00Z")
        Instant accessTokenExpiresAt,

        @Schema(description = "Refresh token", example = "5f0a8f42-7c76-4e71-a1db-9f39a7d5d4b8")
        String refreshToken,

        @Schema(description = "Refresh token expiration timestamp", example = "2026-08-01T10:00:00Z")
        Instant refreshTokenExpiresAt,

        @Schema(description = "Authenticated user")
        UserResponse user
) {
}
