package ru.otus.homevault.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Audit event")
public record AuditEventResponse(
        @Schema(description = "Audit event id", example = "35ad0066-8b2b-4e0b-a11d-d31d54664174")
        UUID id,

        @Schema(description = "Actor user id. Null for public/anonymous events.", example = "755aa608-f4b9-4bb7-a3d4-b3d74f679047")
        UUID actorUserId,

        @Schema(description = "Action", example = "FILE_UPLOADED")
        String action,

        @Schema(description = "Entity type", example = "FILE")
        String entityType,

        @Schema(description = "Entity id", example = "a8b6c4f9-3bc8-4f5d-87ea-d5c99cb91f0a")
        UUID entityId,

        @Schema(description = "Client IP address", example = "127.0.0.1")
        String ipAddress,

        @Schema(description = "Client user agent", example = "Mozilla/5.0")
        String userAgent,

        @Schema(description = "Event details")
        Map<String, Object> details,

        @Schema(description = "Creation timestamp", example = "2026-07-18T10:00:00Z")
        Instant createdAt
) {
}
