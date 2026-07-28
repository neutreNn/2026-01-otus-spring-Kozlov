package ru.otus.homevault.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Common REST API error response")
public record ApiErrorResponse(
        @Schema(description = "Error timestamp in UTC", example = "2026-07-18T10:00:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable error message", example = "Validation failed")
        String message,

        @Schema(description = "Request path", example = "/api/v1/files")
        String path,

        @Schema(description = "Additional structured error details")
        Map<String, Object> details
) {
}

