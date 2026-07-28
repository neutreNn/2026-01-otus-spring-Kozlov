package ru.otus.homevault.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.homevault.common.config.OpenApiConfig;
import ru.otus.homevault.common.dto.ApiErrorResponse;
import ru.otus.homevault.common.security.AuthenticatedUser;
import ru.otus.homevault.common.security.UserOnly;
import ru.otus.homevault.users.dto.ChangePasswordRequest;
import ru.otus.homevault.users.dto.UpdateCurrentUserRequest;
import ru.otus.homevault.users.dto.UserResponse;
import ru.otus.homevault.users.service.UsersService;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Current user profile management")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@UserOnly
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me")
    @Operation(summary = "Current profile", description = "Returns the current user's profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user profile"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return usersService.getCurrentUser(currentUser.id());
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current profile", description = "Updates current user's display name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated user profile"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public UserResponse updateMe(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateCurrentUserRequest request
    ) {
        return usersService.updateCurrentUser(currentUser.id(), request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change password", description = "Changes current user's password and revokes active refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Current password is invalid or request validation failed",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public void changePassword(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        usersService.changePassword(currentUser.id(), request);
    }
}

