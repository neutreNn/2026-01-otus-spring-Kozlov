package ru.otus.homevault.admin.api;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.homevault.admin.dto.AdminStatsResponse;
import ru.otus.homevault.admin.dto.UpdateUserStatusRequest;
import ru.otus.homevault.admin.service.AdminService;
import ru.otus.homevault.audit.dto.AuditEventResponse;
import ru.otus.homevault.audit.service.AuditContext;
import ru.otus.homevault.audit.service.AuditService;
import ru.otus.homevault.common.config.OpenApiConfig;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.common.dto.PageResponse;
import ru.otus.homevault.common.security.AdminOnly;
import ru.otus.homevault.common.security.AuthenticatedUser;
import ru.otus.homevault.users.dto.UserResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrative users, audit and statistics")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@AdminOnly
public class AdminController {

    private final AdminService adminService;

    private final AuditService auditService;

    public AdminController(AdminService adminService, AuditService auditService) {
        this.adminService = adminService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    @Operation(summary = "List users", description = "Returns all users for administration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users page"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin role required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public PageResponse<UserResponse> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return adminService.listUsers(pageable);
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user status", description = "Blocks or unblocks a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated user"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public UserResponse updateUserStatus(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        return adminService.updateUserStatus(currentUser.id(), userId, request, AuditContext.from(httpRequest));
    }

    @GetMapping("/stats")
    @Operation(summary = "System stats", description = "Returns storage, notes, shares and users statistics.")
    @ApiResponse(responseCode = "200", description = "System statistics")
    public AdminStatsResponse stats() {
        return adminService.getStats();
    }

    @GetMapping("/audit/events")
    @Operation(summary = "List audit events", description = "Returns audit events with optional admin filters.")
    @ApiResponse(responseCode = "200", description = "Audit events page")
    public PageResponse<AuditEventResponse> listAuditEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return auditService.listAdminEvents(userId, action, pageable);
    }
}
