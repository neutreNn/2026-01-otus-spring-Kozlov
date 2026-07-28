package ru.otus.homevault.auth.dto;

import java.time.Instant;

public record RefreshTokenResult(String token, Instant expiresAt) {
}

