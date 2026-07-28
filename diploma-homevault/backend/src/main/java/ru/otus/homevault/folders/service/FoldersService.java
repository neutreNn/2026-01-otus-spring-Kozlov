package ru.otus.homevault.folders.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.audit.service.AuditService;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.folders.dto.CreateFolderRequest;
import ru.otus.homevault.folders.dto.FolderResponse;
import ru.otus.homevault.folders.dto.UpdateFolderRequest;
import ru.otus.homevault.folders.model.Folder;
import ru.otus.homevault.folders.repository.FolderRepository;
import ru.otus.homevault.storage.repository.StoredFileRepository;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.util.Map;
import java.util.UUID;

@Service
public class FoldersService {

    private final FolderRepository folderRepository;

    private final StoredFileRepository storedFileRepository;

    private final UserRepository userRepository;

    private final FolderMapper folderMapper;

    private final AuditService auditService;

    public FoldersService(
            FolderRepository folderRepository,
            StoredFileRepository storedFileRepository,
            UserRepository userRepository,
            FolderMapper folderMapper,
            AuditService auditService
    ) {
        this.folderRepository = folderRepository;
        this.storedFileRepository = storedFileRepository;
        this.userRepository = userRepository;
        this.folderMapper = folderMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<FolderResponse> listFolders(UUID userId, UUID parentId, Pageable pageable) {
        if (parentId != null) {
            findOwnedFolder(userId, parentId);
        }

        Page<FolderResponse> folders = (parentId == null
                ? folderRepository.findByOwner_IdAndParentIsNull(userId, pageable)
                : folderRepository.findByOwner_IdAndParent_Id(userId, parentId, pageable))
                .map(folderMapper::toResponse);

        return PageResponse.from(folders);
    }

    @Transactional
    public FolderResponse createFolder(UUID userId, CreateFolderRequest request, AuditContext auditContext) {
        String name = normalizeFolderName(request.name());
        Folder parent = request.parentId() == null ? null : findOwnedFolder(userId, request.parentId());
        ensureFolderNameIsUnique(userId, parent, name, null);

        User owner = userRepository.getReferenceById(userId);
        Folder folder = folderRepository.saveAndFlush(new Folder(owner, parent, name));
        auditService.record(
                userId,
                AuditService.FOLDER_CREATED,
                AuditService.ENTITY_FOLDER,
                folder.getId(),
                auditContext,
                Map.of("name", folder.getName())
        );

        return folderMapper.toResponse(folder);
    }

    @Transactional
    public FolderResponse updateFolder(UUID userId, UUID folderId, UpdateFolderRequest request) {
        Folder folder = findOwnedFolder(userId, folderId);
        String newName = request.name() == null ? folder.getName() : normalizeFolderName(request.name());
        Folder newParent = request.hasParentId()
                ? request.parentId() == null ? null : findOwnedFolder(userId, request.parentId())
                : folder.getParent();

        if (newParent != null && newParent.getId().equals(folder.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Folder cannot be moved into itself");
        }
        ensureParentIsNotDescendant(folder, newParent);
        ensureFolderNameIsUnique(userId, newParent, newName, folder.getId());

        folder.setName(newName);
        folder.setParent(newParent);
        return folderMapper.toResponse(folder);
    }

    @Transactional
    public void deleteFolder(UUID userId, UUID folderId, AuditContext auditContext) {
        Folder folder = findOwnedFolder(userId, folderId);
        if (folderRepository.existsByParent_Id(folderId) || storedFileRepository.existsByFolder_Id(folderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder is not empty");
        }

        folderRepository.delete(folder);
        auditService.record(
                userId,
                AuditService.FOLDER_DELETED,
                AuditService.ENTITY_FOLDER,
                folderId,
                auditContext,
                Map.of("name", folder.getName())
        );
    }

    private Folder findOwnedFolder(UUID userId, UUID folderId) {
        return folderRepository.findByIdAndOwner_Id(folderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    private void ensureParentIsNotDescendant(Folder folder, Folder newParent) {
        Folder current = newParent;
        while (current != null) {
            if (current.getId().equals(folder.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Folder cannot be moved into its child folder");
            }
            current = current.getParent();
        }
    }

    private void ensureFolderNameIsUnique(UUID userId, Folder parent, String name, UUID currentFolderId) {
        boolean exists = parent == null
                ? folderNameExistsInRoot(userId, name, currentFolderId)
                : folderNameExistsInParent(userId, parent.getId(), name, currentFolderId);

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder name already exists in this location");
        }
    }

    private boolean folderNameExistsInRoot(UUID userId, String name, UUID currentFolderId) {
        if (currentFolderId == null) {
            return folderRepository.existsByOwner_IdAndParentIsNullAndNameIgnoreCase(userId, name);
        }
        return folderRepository.existsByOwner_IdAndParentIsNullAndNameIgnoreCaseAndIdNot(userId, name, currentFolderId);
    }

    private boolean folderNameExistsInParent(UUID userId, UUID parentId, String name, UUID currentFolderId) {
        if (currentFolderId == null) {
            return folderRepository.existsByOwner_IdAndParent_IdAndNameIgnoreCase(userId, parentId, name);
        }
        return folderRepository.existsByOwner_IdAndParent_IdAndNameIgnoreCaseAndIdNot(
                userId,
                parentId,
                name,
                currentFolderId
        );
    }

    private String normalizeFolderName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Folder name must not be blank");
        }
        if (normalized.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Folder name is too long");
        }
        return normalized;
    }
}
