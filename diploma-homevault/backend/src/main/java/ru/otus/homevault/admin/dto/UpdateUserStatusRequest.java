package ru.otus.homevault.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.otus.homevault.users.model.UserStatus;

@Schema(description = "User status update request")
public record UpdateUserStatusRequest(
        @Schema(description = "New user status", example = "BLOCKED")
        @NotNull
        UserStatus status
) {
}
