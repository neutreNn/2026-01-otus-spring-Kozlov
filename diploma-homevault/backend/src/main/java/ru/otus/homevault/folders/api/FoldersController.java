package ru.otus.homevault.folders.api;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import ru.otus.homevault.folders.dto.CreateFolderRequest;
import ru.otus.homevault.folders.dto.FolderResponse;
import ru.otus.homevault.folders.dto.UpdateFolderRequest;
import ru.otus.homevault.folders.service.FoldersService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/folders")
@Tag(name = "Folders", description = "Folder tree management")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@UserOnly
public class FoldersController {

    private final FoldersService foldersService;

    public FoldersController(FoldersService foldersService) {
        this.foldersService = foldersService;
    }

    @GetMapping
    @Operation(summary = "List folders", description = "Returns folders owned by current user for the selected parent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folders page"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent folder not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PageResponse<FolderResponse> listFolders(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID parentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return foldersService.listFolders(currentUser.id(), parentId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create folder", description = "Creates a folder in root or inside another owned folder.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Folder created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Folder with this name already exists in selected parent",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public FolderResponse createFolder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateFolderRequest request,
            HttpServletRequest httpRequest
    ) {
        return foldersService.createFolder(currentUser.id(), request, AuditContext.from(httpRequest));
    }

    @PatchMapping("/{folderId}")
    @Operation(summary = "Update folder", description = "Renames a folder or moves it into another owned folder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder updated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public FolderResponse updateFolder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID folderId,
            @Valid @RequestBody UpdateFolderRequest request
    ) {
        return foldersService.updateFolder(currentUser.id(), folderId, request);
    }

    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete folder", description = "Deletes an empty owned folder.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Folder deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Folder is not empty",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public void deleteFolder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID folderId,
            HttpServletRequest httpRequest
    ) {
        foldersService.deleteFolder(currentUser.id(), folderId, AuditContext.from(httpRequest));
    }
}
