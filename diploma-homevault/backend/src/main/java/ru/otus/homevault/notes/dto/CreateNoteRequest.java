package ru.otus.homevault.notes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Note creation request")
public record CreateNoteRequest(
        @Schema(description = "Note title", example = "Deployment checklist")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Note content", example = "1. Build image\n2. Run migrations\n3. Check health")
        @NotBlank
        String content,

        @Schema(description = "Note tags. Values are trimmed and lowercased.", example = "[\"work\", \"devops\"]")
        @Size(max = 50)
        Set<@Size(max = 80) String> tags
) {
}
