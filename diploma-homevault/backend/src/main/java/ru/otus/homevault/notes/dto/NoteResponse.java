package ru.otus.homevault.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Note")
public record NoteResponse(
        @Schema(description = "Note id", example = "19ef7636-77d8-4dd9-9e9d-fec57bd41581")
        UUID id,

        @Schema(description = "Note title", example = "Deployment checklist")
        String title,

        @Schema(description = "Note content", example = "1. Build image\n2. Run migrations\n3. Check health")
        String content,

        @Schema(description = "Normalized tags", example = "[\"devops\", \"work\"]")
        Set<String> tags,

        @Schema(description = "Creation timestamp", example = "2026-07-18T10:00:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-07-18T10:05:00Z")
        Instant updatedAt
) {
}
