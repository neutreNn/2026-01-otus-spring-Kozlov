package ru.otus.homevault.sharing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.sharing.model.ShareResourceType;
import ru.otus.homevault.storage.dto.FileResponse;

import java.time.Instant;

@Schema(description = "Publicly shared resource")
public record PublicShareResponse(
        @Schema(description = "Public token", example = "9f4e7b1c2a3d4e5f")
        String token,

        @Schema(description = "Resource type", example = "FILE")
        ShareResourceType resourceType,

        @Schema(description = "Expiration timestamp", example = "2026-07-19T10:00:00Z")
        Instant expiresAt,

        @Schema(description = "Public access count after this request", example = "3")
        long accessCount,

        @Schema(description = "File metadata when resourceType is FILE")
        FileResponse file,

        @Schema(description = "Note content when resourceType is NOTE")
        NoteResponse note
) {
}
