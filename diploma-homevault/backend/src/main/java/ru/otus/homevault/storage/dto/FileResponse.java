package ru.otus.homevault.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "File metadata")
public record FileResponse(
        @Schema(description = "File id", example = "a8b6c4f9-3bc8-4f5d-87ea-d5c99cb91f0a")
        UUID id,

        @Schema(description = "Folder id. Null for root files.", example = "c6b184b3-58f4-42ac-964a-f256a2346a18")
        UUID folderId,

        @Schema(description = "Original file name", example = "report.pdf")
        String originalName,

        @Schema(description = "Content type", example = "application/pdf")
        String contentType,

        @Schema(description = "File size in bytes", example = "1048576")
        long sizeBytes,

        @Schema(description = "SHA-256 checksum", example = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
        String checksumSha256,

        @Schema(description = "Creation timestamp", example = "2026-07-18T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-07-18T10:05:00Z")
        Instant updatedAt
) {
}
