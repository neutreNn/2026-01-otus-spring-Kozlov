package ru.otus.homevault.storage.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.audit.service.AuditService;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.folders.model.Folder;
import ru.otus.homevault.folders.repository.FolderRepository;
import ru.otus.homevault.storage.dto.FileDownload;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.storage.dto.UpdateFileRequest;
import ru.otus.homevault.storage.exception.StorageException;
import ru.otus.homevault.storage.model.StoredFile;
import ru.otus.homevault.storage.repository.StoredFileRepository;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class FilesService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final int STORAGE_NAME_LENGTH = 120;

    private final StoredFileRepository storedFileRepository;

    private final FolderRepository folderRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;

    private final FileMapper fileMapper;

    private final AuditService auditService;

    public FilesService(
            StoredFileRepository storedFileRepository,
            FolderRepository folderRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            FileMapper fileMapper,
            AuditService auditService
    ) {
        this.storedFileRepository = storedFileRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.fileMapper = fileMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<FileResponse> listFiles(UUID userId, UUID folderId, Pageable pageable) {
        if (folderId != null) {
            findOwnedFolder(userId, folderId);
        }

        Page<FileResponse> files = (folderId == null
                ? storedFileRepository.findByOwner_IdAndFolderIsNull(userId, pageable)
                : storedFileRepository.findByOwner_IdAndFolder_Id(userId, folderId, pageable))
                .map(fileMapper::toResponse);

        return PageResponse.from(files);
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(UUID userId, UUID fileId) {
        return fileMapper.toResponse(findOwnedFile(userId, fileId));
    }

    @Transactional
    public FileResponse uploadFile(UUID userId, UUID folderId, MultipartFile multipartFile, AuditContext auditContext) {
        if (multipartFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String originalName = normalizeOriginalName(multipartFile.getOriginalFilename());
        String contentType = resolveContentType(multipartFile.getContentType());
        Folder folder = folderId == null ? null : findOwnedFolder(userId, folderId);
        String storageKey = createStorageKey(userId, originalName);
        String checksum = uploadToStorage(storageKey, multipartFile, contentType);

        User owner = userRepository.getReferenceById(userId);
        StoredFile storedFile = new StoredFile(
                owner,
                folder,
                originalName,
                storageKey,
                contentType,
                multipartFile.getSize(),
                checksum
        );

        try {
            StoredFile savedFile = storedFileRepository.saveAndFlush(storedFile);
            auditService.record(
                    userId,
                    AuditService.FILE_UPLOADED,
                    AuditService.ENTITY_FILE,
                    savedFile.getId(),
                    auditContext,
                    Map.of(
                            "originalName", savedFile.getOriginalName(),
                            "sizeBytes", savedFile.getSizeBytes()
                    )
            );
            return fileMapper.toResponse(savedFile);
        } catch (RuntimeException exception) {
            deleteStorageObjectQuietly(storageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public FileDownload downloadFile(UUID userId, UUID fileId, AuditContext auditContext) {
        StoredFile storedFile = findOwnedFile(userId, fileId);
        InputStream content = fileStorageService.get(storedFile.getStorageKey());
        auditService.record(
                userId,
                AuditService.FILE_DOWNLOADED,
                AuditService.ENTITY_FILE,
                storedFile.getId(),
                auditContext,
                Map.of("originalName", storedFile.getOriginalName())
        );

        return new FileDownload(
                storedFile.getId(),
                storedFile.getOriginalName(),
                storedFile.getContentType(),
                storedFile.getSizeBytes(),
                content
        );
    }

    @Transactional
    public FileResponse updateFile(UUID userId, UUID fileId, UpdateFileRequest request) {
        StoredFile storedFile = findOwnedFile(userId, fileId);
        if (request.originalName() != null) {
            storedFile.setOriginalName(normalizeOriginalName(request.originalName()));
        }
        if (request.hasFolderId()) {
            Folder folder = request.folderId() == null ? null : findOwnedFolder(userId, request.folderId());
            storedFile.setFolder(folder);
        }
        return fileMapper.toResponse(storedFile);
    }

    @Transactional
    public void deleteFile(UUID userId, UUID fileId, AuditContext auditContext) {
        StoredFile storedFile = findOwnedFile(userId, fileId);
        fileStorageService.delete(storedFile.getStorageKey());
        storedFileRepository.delete(storedFile);
        auditService.record(
                userId,
                AuditService.FILE_DELETED,
                AuditService.ENTITY_FILE,
                fileId,
                auditContext,
                Map.of("originalName", storedFile.getOriginalName())
        );
    }

    private StoredFile findOwnedFile(UUID userId, UUID fileId) {
        return storedFileRepository.findByIdAndOwner_Id(fileId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    private Folder findOwnedFolder(UUID userId, UUID folderId) {
        return folderRepository.findByIdAndOwner_Id(folderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    private String uploadToStorage(String storageKey, MultipartFile multipartFile, String contentType) {
        MessageDigest digest = sha256();
        try (InputStream rawContent = multipartFile.getInputStream();
             DigestInputStream content = new DigestInputStream(rawContent, digest)) {
            fileStorageService.put(storageKey, content, multipartFile.getSize(), contentType);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read uploaded file", exception);
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private String createStorageKey(UUID userId, String originalName) {
        String safeName = originalName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!StringUtils.hasText(safeName)) {
            safeName = "uploaded-file";
        }
        if (safeName.length() > STORAGE_NAME_LENGTH) {
            safeName = safeName.substring(safeName.length() - STORAGE_NAME_LENGTH);
        }

        return userId + "/" + UUID.randomUUID() + "/" + safeName;
    }

    private String normalizeOriginalName(String originalName) {
        String fileName = StringUtils.getFilename(originalName == null ? "" : originalName);
        String normalized = fileName == null ? "" : fileName.trim();
        if (!StringUtils.hasText(normalized)) {
            normalized = "uploaded-file";
        }
        if (normalized.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is too long");
        }
        return normalized;
    }

    private String resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return DEFAULT_CONTENT_TYPE;
        }
        return contentType;
    }

    private void deleteStorageObjectQuietly(String storageKey) {
        try {
            fileStorageService.delete(storageKey);
        } catch (StorageException exception) {
            // Best effort cleanup after metadata save failure.
        }
    }
}
