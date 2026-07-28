package ru.otus.homevault.folders.service;

import org.springframework.stereotype.Component;
import ru.otus.homevault.folders.dto.FolderResponse;
import ru.otus.homevault.folders.model.Folder;

import java.util.UUID;

@Component
public class FolderMapper {

    public FolderResponse toResponse(Folder folder) {
        UUID parentId = folder.getParent() == null ? null : folder.getParent().getId();
        return new FolderResponse(
                folder.getId(),
                parentId,
                folder.getName(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
