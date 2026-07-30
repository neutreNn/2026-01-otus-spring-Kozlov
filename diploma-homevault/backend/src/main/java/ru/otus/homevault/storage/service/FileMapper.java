package ru.otus.homevault.storage.service;

import org.springframework.stereotype.Component;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.storage.model.StoredFile;

import java.util.UUID;

@Component
public class FileMapper {

    public FileResponse toResponse(StoredFile file) {
        UUID folderId = file.getFolder() == null ? null : file.getFolder().getId();
        return new FileResponse(
                file.getId(),
                folderId,
                file.getOriginalName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getChecksumSha256(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}
