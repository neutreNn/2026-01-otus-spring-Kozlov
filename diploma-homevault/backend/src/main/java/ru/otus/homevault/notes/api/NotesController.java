package ru.otus.homevault.notes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.common.config.OpenApiConfig;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.common.security.AuthenticatedUser;
import ru.otus.homevault.common.security.UserOnly;
import ru.otus.homevault.notes.dto.CreateNoteRequest;
import ru.otus.homevault.notes.dto.NoteResponse;
import ru.otus.homevault.notes.dto.UpdateNoteRequest;
import ru.otus.homevault.notes.service.NotesService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "Notes", description = "Personal notes, tags and search")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@UserOnly
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping
    @Operation(summary = "List and search notes", description = "Searches current user's notes by title/content and tag.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notes page"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PageResponse<NoteResponse> listNotes(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return notesService.listNotes(currentUser.id(), query, tag, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create note", description = "Creates a personal note with normalized tags.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NoteResponse createNote(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateNoteRequest request,
            HttpServletRequest httpRequest
    ) {
        return notesService.createNote(currentUser.id(), request, AuditContext.from(httpRequest));
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Get note", description = "Returns one owned note.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NoteResponse getNote(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID noteId
    ) {
        return notesService.getNote(currentUser.id(), noteId);
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Update note", description = "Fully updates an owned note and replaces its tags.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public NoteResponse updateNote(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteRequest request,
            HttpServletRequest httpRequest
    ) {
        return notesService.updateNote(currentUser.id(), noteId, request, AuditContext.from(httpRequest));
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete note", description = "Deletes an owned note and its tags.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public void deleteNote(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID noteId,
            HttpServletRequest httpRequest
    ) {
        notesService.deleteNote(currentUser.id(), noteId, AuditContext.from(httpRequest));
    }
}
