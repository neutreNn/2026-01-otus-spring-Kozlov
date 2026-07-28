package ru.otus.homevault.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Password change request")
public record ChangePasswordRequest(
        @NotBlank
        @Size(min = 8, max = 128)
        @Schema(description = "Current password", example = "old-password")
        String currentPassword,

        @NotBlank
        @Size(min = 8, max = 128)
        @Schema(description = "New password", example = "new-password")
        String newPassword
) {
}

