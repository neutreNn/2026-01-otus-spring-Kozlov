package ru.otus.homevault.sharing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.homevault.sharing.model.ShareResourceType;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Public share link metadata")
public record ShareResponse(
        @Schema(description = "Share id", example = "0ca1904f-266c-4d54-a641-e93dc3e66ccf")
        UUID id,

        @Schema(description = "Public token", example = "9f4e7b1c2a3d4e5f")
        String token,

        @Schema(description = "Resource type", example = "FILE")
        ShareResourceType resourceType,

        @Schema(description = "Shared resource id", example = "a8b6c4f9-3bc8-4f5d-87ea-d5c99cb91f0a")
        UUID resourceId,

        @Schema(description = "Expiration timestamp", example = "2026-07-19T10:00:00Z")
        Instant expiresAt,

        @Schema(description = "Revocation timestamp", example = "2026-07-18T11:00:00Z")
        Instant revokedAt,

        @Schema(description = "Public access count", example = "3")
        long accessCount,

        @Schema(description = "Creation timestamp", example = "2026-07-18T10:00:00Z")
        Instant createdAt
) {
}
