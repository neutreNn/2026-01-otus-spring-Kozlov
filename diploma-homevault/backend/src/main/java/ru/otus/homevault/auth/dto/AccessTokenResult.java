package ru.otus.homevault.auth.dto;

import java.time.Instant;

public record AccessTokenResult(String token, Instant expiresAt) {
}

