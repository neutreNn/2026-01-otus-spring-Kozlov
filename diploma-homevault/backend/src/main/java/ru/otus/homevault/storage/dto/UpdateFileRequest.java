package ru.otus.homevault.storage.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "File update request")
public class UpdateFileRequest {

    @Schema(description = "New display file name", example = "renamed-report.pdf")
    @Size(max = 255)
    private String originalName;

    @Schema(description = "New folder id. Null moves file to root. Omit to keep current folder.", example = "c6b184b3-58f4-42ac-964a-f256a2346a18")
    private UUID folderId;

    @Schema(hidden = true)
    private boolean folderIdPresent;

    public UpdateFileRequest() {
    }

    public UpdateFileRequest(String originalName, UUID folderId) {
        this.originalName = originalName;
        this.folderId = folderId;
        this.folderIdPresent = true;
    }

    public String originalName() {
        return originalName;
    }

    public UUID folderId() {
        return folderId;
    }

    public boolean hasFolderId() {
        return folderIdPresent;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @JsonSetter("folderId")
    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
        this.folderIdPresent = true;
    }
}
