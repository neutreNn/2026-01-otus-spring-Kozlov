package ru.otus.homevault.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Note update request")
public record UpdateNoteRequest(
        @Schema(description = "Note title", example = "Updated deployment checklist")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Note content", example = "Updated note content")
        @NotBlank
        String content,

        @Schema(description = "Note tags. Values are trimmed and lowercased.", example = "[\"work\", \"ops\"]")
        @Size(max = 50)
        Set<@Size(max = 80) String> tags
) {
}
