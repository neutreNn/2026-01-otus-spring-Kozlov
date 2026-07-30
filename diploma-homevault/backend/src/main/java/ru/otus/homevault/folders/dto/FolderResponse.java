package ru.otus.homevault.folders.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Folder metadata")
public record FolderResponse(
        @Schema(description = "Folder id", example = "c6b184b3-58f4-42ac-964a-f256a2346a18")
        UUID id,

        @Schema(description = "Parent folder id. Null for root folders.", example = "00000000-0000-0000-0000-000000000000")
        UUID parentId,

        @Schema(description = "Folder name", example = "Documents")
        String name,

        @Schema(description = "Creation timestamp", example = "2026-07-18T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-07-18T10:05:00Z")
        Instant updatedAt
) {
}
