package ru.otus.homevault.sharing.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.sharing.dto.PublicShareResponse;
import ru.otus.homevault.sharing.service.SharingService;
import ru.otus.homevault.storage.dto.FileDownload;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/public/shares")
@Tag(name = "Public sharing", description = "Open public share links without authentication")
@SecurityRequirements
public class PublicSharingController {

    private final SharingService sharingService;

    public PublicSharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Open public share", description = "Returns metadata/content for an active public share link.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shared resource"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Share link not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "Share link is expired or revoked",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PublicShareResponse openShare(@PathVariable String token, HttpServletRequest httpRequest) {
        return sharingService.openPublicShare(token, AuditContext.from(httpRequest));
    }

    @GetMapping("/{token}/download")
    @Operation(summary = "Download public file", description = "Downloads a file through an active public share link.")
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
                    description = "Share link or file not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "Share link is expired or revoked",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable String token,
            HttpServletRequest httpRequest
    ) {
        FileDownload download = sharingService.downloadPublicFile(token, AuditContext.from(httpRequest));
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(download.originalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(new InputStreamResource(download.content()));
    }

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (RuntimeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
