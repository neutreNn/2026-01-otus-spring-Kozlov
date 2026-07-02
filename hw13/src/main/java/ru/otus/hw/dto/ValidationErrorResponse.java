package ru.otus.hw.dto;

import java.util.Map;

public record ValidationErrorResponse(String message, Map<String, String> fieldErrors) {
}
