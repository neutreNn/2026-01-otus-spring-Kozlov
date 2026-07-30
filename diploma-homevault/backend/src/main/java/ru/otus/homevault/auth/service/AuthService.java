package ru.otus.homevault.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.auth.dto.AccessTokenResult;
import ru.otus.homevault.auth.dto.AuthResponse;
import ru.otus.homevault.auth.dto.LoginRequest;
import ru.otus.homevault.auth.dto.RefreshTokenRequest;
import ru.otus.homevault.auth.dto.RefreshTokenResult;
import ru.otus.homevault.auth.dto.RegisterRequest;
import ru.otus.homevault.auth.model.RefreshToken;
import ru.otus.homevault.common.security.BlockedUserException;
import ru.otus.homevault.users.dto.UserResponse;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;
import ru.otus.homevault.users.service.UserMapper;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim());
        try {
            User savedUser = userRepository.saveAndFlush(user);
            return issueAuthResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered", exception);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        ensureActive(user);

        return issueAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByRawToken(request.refreshToken())
                .orElseThrow(this::invalidRefreshToken);

        Instant now = Instant.now();
        if (!refreshToken.isActive(now)) {
            throw invalidRefreshToken();
        }

        User user = refreshToken.getUser();
        ensureActive(user);
        refreshTokenService.revoke(refreshToken);

        return issueAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.findByRawToken(refreshTokenValue)
                .ifPresent(refreshTokenService::revoke);
    }

    private AuthResponse issueAuthResponse(User user) {
        AccessTokenResult accessToken = jwtService.createAccessToken(user);
        RefreshTokenResult refreshToken = refreshTokenService.issueFor(user);
        UserResponse userResponse = userMapper.toResponse(user);
        return new AuthResponse(
                "Bearer",
                accessToken.token(),
                accessToken.expiresAt(),
                refreshToken.token(),
                refreshToken.expiresAt(),
                userResponse
        );
    }

    private void ensureActive(User user) {
        if (user.isBlocked()) {
            throw new BlockedUserException();
        }
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

