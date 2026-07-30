package ru.otus.homevault.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "homevault.jwt")
public record JwtProperties(
        String secret,
        long accessTtlMinutes,
        long refreshTtlDays
) {

    public JwtProperties {
        if (accessTtlMinutes <= 0) {
            throw new IllegalArgumentException("JWT access token TTL must be positive");
        }
        if (refreshTtlDays <= 0) {
            throw new IllegalArgumentException("JWT refresh token TTL must be positive");
        }
    }

    public Duration accessTokenTtl() {
        return Duration.ofMinutes(accessTtlMinutes);
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(refreshTtlDays);
    }
}

