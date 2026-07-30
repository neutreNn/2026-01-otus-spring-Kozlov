package ru.otus.homevault.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.homevault.users.model.Role;
import ru.otus.homevault.users.model.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "User profile")
public record UserResponse(
        @Schema(description = "User id", example = "00000000-0000-0000-0000-000000000000")
        UUID id,

        @Schema(description = "User email", example = "user@example.com")
        String email,

        @Schema(description = "Display name", example = "Ivan Petrov")
        String displayName,

        @Schema(description = "User status", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "User roles", example = "[\"USER\"]")
        Set<Role> roles,

        @Schema(description = "Storage limit in bytes")
        Long storageLimitBytes,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
}

