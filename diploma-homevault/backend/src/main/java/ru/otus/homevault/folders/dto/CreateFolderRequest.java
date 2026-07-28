package ru.otus.homevault.folders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Folder creation request")
public record CreateFolderRequest(
        @Schema(description = "Folder name", example = "Documents")
        @NotBlank
        @Size(max = 255)
        String name,

        @Schema(description = "Parent folder id. Null means root folder.", example = "00000000-0000-0000-0000-000000000000")
        UUID parentId
) {
}
