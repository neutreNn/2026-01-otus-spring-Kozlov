package ru.otus.homevault.sharing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.audit.service.AuditService;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.notes.model.Note;
import ru.otus.homevault.notes.repository.NoteRepository;
import ru.otus.homevault.notes.service.NoteMapper;
import ru.otus.homevault.sharing.dto.CreateShareRequest;
import ru.otus.homevault.sharing.dto.PublicShareResponse;
import ru.otus.homevault.sharing.dto.ShareResponse;
import ru.otus.homevault.sharing.model.ShareLink;
import ru.otus.homevault.sharing.model.ShareResourceType;
import ru.otus.homevault.sharing.repository.ShareLinkRepository;
import ru.otus.homevault.storage.dto.FileDownload;
import ru.otus.homevault.storage.model.StoredFile;
import ru.otus.homevault.storage.repository.StoredFileRepository;
import ru.otus.homevault.storage.service.FileMapper;
import ru.otus.homevault.storage.service.FileStorageService;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class SharingService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ShareLinkRepository shareLinkRepository;

    private final StoredFileRepository storedFileRepository;

    private final NoteRepository noteRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;

    private final ShareLinkMapper shareLinkMapper;

    private final FileMapper fileMapper;

    private final NoteMapper noteMapper;

    private final AuditService auditService;

    public SharingService(
            ShareLinkRepository shareLinkRepository,
            StoredFileRepository storedFileRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            ShareLinkMapper shareLinkMapper,
            FileMapper fileMapper,
            NoteMapper noteMapper,
            AuditService auditService
    ) {
        this.shareLinkRepository = shareLinkRepository;
        this.storedFileRepository = storedFileRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.shareLinkMapper = shareLinkMapper;
        this.fileMapper = fileMapper;
        this.noteMapper = noteMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ShareResponse> listShares(UUID userId, Pageable pageable) {
        Page<ShareResponse> shares = shareLinkRepository.findByOwner_Id(userId, pageable)
                .map(shareLinkMapper::toResponse);
        return PageResponse.from(shares);
    }

    @Transactional
    public ShareResponse createShare(UUID userId, CreateShareRequest request, AuditContext auditContext) {
        ensureOwnedResourceExists(userId, request.resourceType(), request.resourceId());

        User owner = userRepository.getReferenceById(userId);
        ShareLink shareLink = new ShareLink(
                owner,
                request.resourceType(),
                request.resourceId(),
                generateUniqueToken(),
                request.expiresAt()
        );

        ShareLink savedShare = shareLinkRepository.saveAndFlush(shareLink);
        auditService.record(
                userId,
                AuditService.SHARE_CREATED,
                AuditService.ENTITY_SHARE_LINK,
                savedShare.getId(),
                auditContext,
                Map.of(
                        "resourceType", savedShare.getResourceType().name(),
                        "resourceId", savedShare.getResourceId().toString(),
                        "expiresAt", savedShare.getExpiresAt().toString()
                )
        );

        return shareLinkMapper.toResponse(savedShare);
    }

    @Transactional
    public void revokeShare(UUID userId, UUID shareId, AuditContext auditContext) {
        ShareLink shareLink = shareLinkRepository.findByIdAndOwner_Id(shareId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));

        if (shareLink.getRevokedAt() == null) {
            shareLink.setRevokedAt(Instant.now());
            auditService.record(
                    userId,
                    AuditService.SHARE_REVOKED,
                    AuditService.ENTITY_SHARE_LINK,
                    shareLink.getId(),
                    auditContext,
                    Map.of("token", shareLink.getToken())
            );
        }
    }

    @Transactional
    public PublicShareResponse openPublicShare(String token, AuditContext auditContext) {
        ShareLink shareLink = findActiveShareByToken(token);
        shareLink.incrementAccessCount();

        PublicShareResponse response = switch (shareLink.getResourceType()) {
            case FILE -> openFileShare(shareLink);
            case NOTE -> openNoteShare(shareLink);
        };

        auditService.record(
                null,
                AuditService.SHARE_ACCESSED,
                AuditService.ENTITY_SHARE_LINK,
                shareLink.getId(),
                auditContext,
                Map.of("token", shareLink.getToken(), "resourceType", shareLink.getResourceType().name())
        );
        return response;
    }

    @Transactional
    public FileDownload downloadPublicFile(String token, AuditContext auditContext) {
        ShareLink shareLink = findActiveShareByToken(token);
        if (shareLink.getResourceType() != ShareResourceType.FILE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Share link does not reference a file");
        }

        StoredFile storedFile = storedFileRepository.findById(shareLink.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared file not found"));

        shareLink.incrementAccessCount();
        auditService.record(
                null,
                AuditService.SHARE_FILE_DOWNLOADED,
                AuditService.ENTITY_SHARE_LINK,
                shareLink.getId(),
                auditContext,
                Map.of("token", shareLink.getToken(), "fileId", storedFile.getId().toString())
        );

        return new FileDownload(
                storedFile.getId(),
                storedFile.getOriginalName(),
                storedFile.getContentType(),
                storedFile.getSizeBytes(),
                fileStorageService.get(storedFile.getStorageKey())
        );
    }

    private PublicShareResponse openFileShare(ShareLink shareLink) {
        StoredFile storedFile = storedFileRepository.findById(shareLink.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared file not found"));
        return new PublicShareResponse(
                shareLink.getToken(),
                shareLink.getResourceType(),
                shareLink.getExpiresAt(),
                shareLink.getAccessCount(),
                fileMapper.toResponse(storedFile),
                null
        );
    }

    private PublicShareResponse openNoteShare(ShareLink shareLink) {
        Note note = noteRepository.findById(shareLink.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared note not found"));
        return new PublicShareResponse(
                shareLink.getToken(),
                shareLink.getResourceType(),
                shareLink.getExpiresAt(),
                shareLink.getAccessCount(),
                null,
                noteMapper.toResponse(note)
        );
    }

    private ShareLink findActiveShareByToken(String token) {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));

        if (shareLink.isRevoked() || shareLink.isExpired(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Share link is no longer available");
        }
        return shareLink;
    }

    private void ensureOwnedResourceExists(UUID userId, ShareResourceType resourceType, UUID resourceId) {
        boolean exists = switch (resourceType) {
            case FILE -> storedFileRepository.findByIdAndOwner_Id(resourceId, userId).isPresent();
            case NOTE -> noteRepository.findByIdAndOwner_Id(resourceId, userId).isPresent();
        };

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shared resource not found");
        }
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String token = generateToken();
            if (!shareLinkRepository.existsByToken(token)) {
                return token;
            }
        }
        throw new IllegalStateException("Could not generate unique share token");
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
