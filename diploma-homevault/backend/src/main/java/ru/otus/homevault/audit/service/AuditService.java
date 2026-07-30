package ru.otus.homevault.audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.otus.homevault.audit.dto.AuditEventResponse;
import ru.otus.homevault.audit.model.AuditEvent;
import ru.otus.homevault.audit.repository.AuditEventRepository;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    public static final String ENTITY_FOLDER = "FOLDER";
    public static final String ENTITY_FILE = "FILE";
    public static final String ENTITY_NOTE = "NOTE";
    public static final String ENTITY_SHARE_LINK = "SHARE_LINK";
    public static final String ENTITY_USER = "USER";
    public static final String FOLDER_CREATED = "FOLDER_CREATED";
    public static final String FOLDER_DELETED = "FOLDER_DELETED";
    public static final String FILE_UPLOADED = "FILE_UPLOADED";
    public static final String FILE_DOWNLOADED = "FILE_DOWNLOADED";
    public static final String FILE_DELETED = "FILE_DELETED";
    public static final String NOTE_CREATED = "NOTE_CREATED";
    public static final String NOTE_UPDATED = "NOTE_UPDATED";
    public static final String NOTE_DELETED = "NOTE_DELETED";
    public static final String SHARE_CREATED = "SHARE_CREATED";
    public static final String SHARE_REVOKED = "SHARE_REVOKED";
    public static final String SHARE_ACCESSED = "SHARE_ACCESSED";
    public static final String SHARE_FILE_DOWNLOADED = "SHARE_FILE_DOWNLOADED";
    public static final String USER_STATUS_UPDATED = "USER_STATUS_UPDATED";

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;

    private final UserRepository userRepository;

    private final TransactionTemplate transactionTemplate;

    public AuditService(
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(
            UUID actorUserId,
            String action,
            String entityType,
            UUID entityId,
            AuditContext context,
            Map<String, Object> details
    ) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                User actor = actorUserId == null ? null : userRepository.getReferenceById(actorUserId);
                AuditEvent event = new AuditEvent(
                        actor,
                        action,
                        entityType,
                        entityId,
                        trim(context == null ? null : context.ipAddress(), 80),
                        trim(context == null ? null : context.userAgent(), 500),
                        details
                );
                auditEventRepository.saveAndFlush(event);
            });
        } catch (RuntimeException exception) {
            log.warn("Failed to record audit event action={} entityType={} entityId={}",
                    action, entityType, entityId, exception);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> listUserEvents(UUID actorUserId, Pageable pageable) {
        Page<AuditEventResponse> events = auditEventRepository
                .findAll(actorEquals(actorUserId), pageable)
                .map(this::toResponse);
        return PageResponse.from(events);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> listAdminEvents(UUID actorUserId, String action, Pageable pageable) {
        Specification<AuditEvent> specification = Specification.where(null);
        if (actorUserId != null) {
            specification = specification.and(actorEquals(actorUserId));
        }
        if (action != null && !action.isBlank()) {
            String normalizedAction = action.trim();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("action"), normalizedAction));
        }

        Page<AuditEventResponse> events = auditEventRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
        return PageResponse.from(events);
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private Specification<AuditEvent> actorEquals(UUID actorUserId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("actor").get("id"), actorUserId);
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        UUID actorUserId = event.getActor() == null ? null : event.getActor().getId();
        return new AuditEventResponse(
                event.getId(),
                actorUserId,
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getIpAddress(),
                event.getUserAgent(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}
