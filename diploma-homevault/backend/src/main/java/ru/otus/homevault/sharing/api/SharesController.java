package ru.otus.homevault.sharing.api;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.common.config.OpenApiConfig;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.common.security.AuthenticatedUser;
import ru.otus.homevault.common.security.UserOnly;
import ru.otus.homevault.sharing.dto.CreateShareRequest;
import ru.otus.homevault.sharing.dto.ShareResponse;
import ru.otus.homevault.sharing.service.SharingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shares")
@Tag(name = "Sharing", description = "Manage own public share links")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@UserOnly
public class SharesController {

    private final SharingService sharingService;

    public SharesController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @GetMapping
    @Operation(summary = "List share links", description = "Returns current user's public share links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share links page"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PageResponse<ShareResponse> listShares(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return sharingService.listShares(currentUser.id(), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create share link", description = "Creates a public link for an owned file or note.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Share link created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shared resource not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ShareResponse createShare(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateShareRequest request,
            HttpServletRequest httpRequest
    ) {
        return sharingService.createShare(currentUser.id(), request, AuditContext.from(httpRequest));
    }

    @DeleteMapping("/{shareId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke share link", description = "Revokes an own public share link.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Share link revoked"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Share link not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public void revokeShare(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID shareId,
            HttpServletRequest httpRequest
    ) {
        sharingService.revokeShare(currentUser.id(), shareId, AuditContext.from(httpRequest));
    }
}
