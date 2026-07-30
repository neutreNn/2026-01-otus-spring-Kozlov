package ru.otus.homevault.folders.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Folder update request")
public class UpdateFolderRequest {

    @Schema(description = "New folder name", example = "Archive")
    @Size(max = 255)
    private String name;

    @Schema(description = "New parent folder id. Null moves folder to root. Omit to keep current parent.", example = "c6b184b3-58f4-42ac-964a-f256a2346a18")
    private UUID parentId;

    @Schema(hidden = true)
    private boolean parentIdPresent;

    public UpdateFolderRequest() {
    }

    public UpdateFolderRequest(String name, UUID parentId) {
        this.name = name;
        this.parentId = parentId;
        this.parentIdPresent = true;
    }

    public String name() {
        return name;
    }

    public UUID parentId() {
        return parentId;
    }

    public boolean hasParentId() {
        return parentIdPresent;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("parentId")
    public void setParentId(UUID parentId) {
        this.parentId = parentId;
        this.parentIdPresent = true;
    }
}
