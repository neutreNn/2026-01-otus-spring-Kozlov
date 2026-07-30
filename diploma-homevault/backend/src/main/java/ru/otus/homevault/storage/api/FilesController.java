package ru.otus.homevault.storage.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.common.config.OpenApiConfig;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.common.security.AuthenticatedUser;
import ru.otus.homevault.common.security.UserOnly;
import ru.otus.homevault.storage.dto.FileDownload;
import ru.otus.homevault.storage.dto.FileResponse;
import ru.otus.homevault.storage.dto.FileUploadForm;
import ru.otus.homevault.storage.dto.UpdateFileRequest;
import ru.otus.homevault.storage.service.FilesService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "File upload, metadata and download")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@UserOnly
public class FilesController {

    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    @GetMapping
    @Operation(summary = "List files", description = "Returns files owned by current user for the selected folder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Files page"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PageResponse<FileResponse> listFiles(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID folderId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return filesService.listFiles(currentUser.id(), folderId, pageable);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload file",
            description = "Uploads a file to MinIO and stores metadata in PostgreSQL.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = FileUploadForm.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File uploaded"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid multipart request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "File is too large",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public FileResponse uploadFile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "File content", required = true)
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) UUID folderId,
            HttpServletRequest httpRequest
    ) {
        return filesService.uploadFile(currentUser.id(), folderId, file, AuditContext.from(httpRequest));
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Get file metadata", description = "Returns metadata for an owned file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File metadata"),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public FileResponse getFile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID fileId
    ) {
        return filesService.getFile(currentUser.id(), fileId);
    }

    @GetMapping("/{fileId}/download")
    @Operation(summary = "Download file", description = "Streams an owned file from object storage.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Binary file content",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InputStreamResource> downloadFile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID fileId,
            HttpServletRequest httpRequest
    ) {
        FileDownload download = filesService.downloadFile(currentUser.id(), fileId, AuditContext.from(httpRequest));

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(download.originalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(new InputStreamResource(download.content()));
    }

    @PatchMapping("/{fileId}")
    @Operation(summary = "Update file metadata", description = "Renames an owned file or moves it into another owned folder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File metadata updated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public FileResponse updateFile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID fileId,
            @Valid @RequestBody UpdateFileRequest request
    ) {
        return filesService.updateFile(currentUser.id(), fileId, request);
    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete file", description = "Deletes file metadata and removes the object from storage.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "File deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "File not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public void deleteFile(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID fileId,
            HttpServletRequest httpRequest
    ) {
        filesService.deleteFile(currentUser.id(), fileId, AuditContext.from(httpRequest));
    }

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (RuntimeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
