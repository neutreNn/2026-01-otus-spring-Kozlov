package ru.otus.homevault.common.web;

import org.springframework.http.HttpStatus;
import ru.otus.homevault.common.dto.ApiErrorResponse;

import java.time.Instant;
import java.util.Map;

public final class ApiErrorResponses {

    private ApiErrorResponses() {
    }

    public static ApiErrorResponse of(HttpStatus status, String message, String path, Map<String, Object> details) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details == null ? Map.of() : Map.copyOf(details)
        );
    }
}

