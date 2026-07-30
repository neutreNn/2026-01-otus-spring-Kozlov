package ru.otus.homevault.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.admin.dto.AdminStatsResponse;
import ru.otus.homevault.admin.dto.UpdateUserStatusRequest;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.audit.service.AuditService;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.folders.repository.FolderRepository;
import ru.otus.homevault.notes.repository.NoteRepository;
import ru.otus.homevault.sharing.repository.ShareLinkRepository;
import ru.otus.homevault.storage.repository.StoredFileRepository;
import ru.otus.homevault.users.dto.UserResponse;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.model.UserStatus;
import ru.otus.homevault.users.repository.UserRepository;
import ru.otus.homevault.users.service.UserMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    private final FolderRepository folderRepository;

    private final StoredFileRepository storedFileRepository;

    private final NoteRepository noteRepository;

    private final ShareLinkRepository shareLinkRepository;

    private final UserMapper userMapper;

    private final AuditService auditService;

    public AdminService(
            UserRepository userRepository,
            FolderRepository folderRepository,
            StoredFileRepository storedFileRepository,
            NoteRepository noteRepository,
            ShareLinkRepository shareLinkRepository,
            UserMapper userMapper,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.storedFileRepository = storedFileRepository;
        this.noteRepository = noteRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Pageable pageable) {
        Page<UserResponse> users = userRepository.findAll(pageable).map(userMapper::toResponse);
        return PageResponse.from(users);
    }

    @Transactional
    public UserResponse updateUserStatus(
            UUID adminUserId,
            UUID userId,
            UpdateUserStatusRequest request,
            AuditContext auditContext
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setStatus(request.status());
        auditService.record(
                adminUserId,
                AuditService.USER_STATUS_UPDATED,
                AuditService.ENTITY_USER,
                userId,
                auditContext,
                Map.of("status", request.status().name())
        );

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        long filesCount = storedFileRepository.count();
        return new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.BLOCKED),
                folderRepository.count(),
                filesCount,
                storedFileRepository.sumSizeBytes(),
                noteRepository.count(),
                shareLinkRepository.count(),
                shareLinkRepository.countActive(Instant.now())
        );
    }
}
