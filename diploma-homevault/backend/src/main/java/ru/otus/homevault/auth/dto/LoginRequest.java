package ru.otus.homevault.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Login request")
public record LoginRequest(
        @Email
        @NotBlank
        @Size(max = 320)
        @Schema(description = "Email", example = "user@example.com")
        String email,

        @NotBlank
        @Size(min = 8, max = 128)
        @Schema(description = "Password", example = "correct-horse-battery-staple")
        String password
) {
}

