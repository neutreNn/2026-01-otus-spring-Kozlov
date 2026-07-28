package ru.otus.homevault.auth.service;

import org.springframework.stereotype.Service;
import ru.otus.homevault.auth.dto.RefreshTokenResult;
import ru.otus.homevault.auth.model.RefreshToken;
import ru.otus.homevault.auth.repository.RefreshTokenRepository;
import ru.otus.homevault.common.security.JwtProperties;
import ru.otus.homevault.users.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTES = 48;

    private final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public RefreshTokenResult issueFor(User user) {
        String token = generateToken();
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenTtl());
        refreshTokenRepository.save(new RefreshToken(user, hash(token), expiresAt));
        return new RefreshTokenResult(token, expiresAt);
    }

    public Optional<RefreshToken> findByRawToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hash(rawToken));
    }

    public void revoke(RefreshToken refreshToken) {
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.revoke(Instant.now());
        }
    }

    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeActiveTokensByUserId(userId, Instant.now());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}

