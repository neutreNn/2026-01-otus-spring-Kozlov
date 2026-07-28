package ru.otus.homevault.notes.service;

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
import ru.otus.homevault.notes.dto.CreateNoteRequest;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.notes.dto.UpdateNoteRequest;
import ru.otus.homevault.notes.model.Note;
import ru.otus.homevault.notes.repository.NoteRepository;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class NotesService {

    private final NoteRepository noteRepository;

    private final UserRepository userRepository;

    private final NoteMapper noteMapper;

    private final AuditService auditService;

    public NotesService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            NoteMapper noteMapper,
            AuditService auditService
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.noteMapper = noteMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<NoteResponse> listNotes(UUID userId, String query, String tag, Pageable pageable) {
        String normalizedQuery = normalizeSearchQuery(query);
        String normalizedTag = normalizeOptionalTag(tag);

        Page<Note> notePage;
        if (normalizedQuery == null && normalizedTag == null) {
            notePage = noteRepository.findByOwner_Id(userId, pageable);
        } else if (normalizedQuery == null) {
            notePage = noteRepository.searchByTag(userId, normalizedTag, pageable);
        } else if (normalizedTag == null) {
            notePage = noteRepository.searchByQuery(userId, normalizedQuery, pageable);
        } else {
            notePage = noteRepository.searchByQueryAndTag(userId, normalizedQuery, normalizedTag, pageable);
        }

        Page<NoteResponse> notes = notePage.map(noteMapper::toResponse);
        return PageResponse.from(notes);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNote(UUID userId, UUID noteId) {
        return noteMapper.toResponse(findOwnedNote(userId, noteId));
    }

    @Transactional
    public NoteResponse createNote(UUID userId, CreateNoteRequest request, AuditContext auditContext) {
        User owner = userRepository.getReferenceById(userId);
        Set<String> tags = normalizeTags(request.tags());
        Note note = new Note(
                owner,
                normalizeTitle(request.title()),
                normalizeContent(request.content()),
                tags
        );

        Note savedNote = noteRepository.saveAndFlush(note);
        auditService.record(
                userId,
                AuditService.NOTE_CREATED,
                AuditService.ENTITY_NOTE,
                savedNote.getId(),
                auditContext,
                Map.of("title", savedNote.getTitle(), "tags", savedNote.getTags())
        );

        return noteMapper.toResponse(savedNote);
    }

    @Transactional
    public NoteResponse updateNote(UUID userId, UUID noteId, UpdateNoteRequest request, AuditContext auditContext) {
        Note note = findOwnedNote(userId, noteId);
        note.setTitle(normalizeTitle(request.title()));
        note.setContent(normalizeContent(request.content()));
        note.setTags(normalizeTags(request.tags()));
        noteRepository.flush();

        auditService.record(
                userId,
                AuditService.NOTE_UPDATED,
                AuditService.ENTITY_NOTE,
                note.getId(),
                auditContext,
                Map.of("title", note.getTitle(), "tags", note.getTags())
        );

        return noteMapper.toResponse(note);
    }

    @Transactional
    public void deleteNote(UUID userId, UUID noteId, AuditContext auditContext) {
        Note note = findOwnedNote(userId, noteId);
        String title = note.getTitle();
        noteRepository.delete(note);
        auditService.record(
                userId,
                AuditService.NOTE_DELETED,
                AuditService.ENTITY_NOTE,
                noteId,
                auditContext,
                Map.of("title", title)
        );
    }

    private Note findOwnedNote(UUID userId, UUID noteId) {
        return noteRepository.findByIdAndOwner_Id(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    private String normalizeTitle(String title) {
        String normalized = title == null ? "" : title.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note title must not be blank");
        }
        if (normalized.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note title is too long");
        }
        return normalized;
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note content must not be blank");
        }
        return content;
    }

    private String normalizeSearchQuery(String query) {
        String normalized = query == null ? null : query.trim();
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String normalizeOptionalTag(String tag) {
        String normalized = tag == null ? null : tag.trim().toLowerCase(Locale.ROOT);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private Set<String> normalizeTags(Collection<String> tags) {
        Set<String> normalizedTags = new TreeSet<>();
        if (tags == null) {
            return normalizedTags;
        }

        for (String tag : tags) {
            String normalized = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            if (normalized.length() > 80) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note tag is too long");
            }
            normalizedTags.add(normalized);
        }
        return normalizedTags;
    }
}
