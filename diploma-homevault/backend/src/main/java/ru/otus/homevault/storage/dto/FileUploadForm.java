package ru.otus.homevault.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Schema(description = "Multipart file upload form")
public class FileUploadForm {

    @Schema(
            description = "Binary file content",
            type = "string",
            format = "binary",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private MultipartFile file;

    @Schema(
            description = "Target folder id. Null means root folder.",
            example = "00000000-0000-0000-0000-000000000000"
    )
    private UUID folderId;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }
}
