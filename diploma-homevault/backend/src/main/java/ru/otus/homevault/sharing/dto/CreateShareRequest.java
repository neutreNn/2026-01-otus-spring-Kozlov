package ru.otus.homevault.sharing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import ru.otus.homevault.sharing.model.ShareResourceType;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Create public share link request")
public record CreateShareRequest(
        @Schema(description = "Resource type", example = "FILE")
        @NotNull
        ShareResourceType resourceType,

        @Schema(description = "Shared resource id", example = "a8b6c4f9-3bc8-4f5d-87ea-d5c99cb91f0a")
        @NotNull
        UUID resourceId,

        @Schema(description = "Expiration timestamp", example = "2026-07-19T10:00:00Z")
        @NotNull
        @Future
        Instant expiresAt
) {
}
