package ru.otus.homevault.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Current user profile update request")
public record UpdateCurrentUserRequest(
        @NotBlank
        @Size(max = 120)
        @Schema(description = "Display name", example = "Ivan Petrov")
        String displayName
) {
}

