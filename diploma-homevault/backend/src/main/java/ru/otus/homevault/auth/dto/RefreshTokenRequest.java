package ru.otus.homevault.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Refresh access token request")
public record RefreshTokenRequest(
        @NotBlank
        @Size(max = 512)
        @Schema(description = "Refresh token", example = "5f0a8f42-7c76-4e71-a1db-9f39a7d5d4b8")
        String refreshToken
) {
}
