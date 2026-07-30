package ru.otus.homevault.auth.dto;

import java.util.UUID;

public record DecodedAccessToken(UUID userId, String email) {
}

