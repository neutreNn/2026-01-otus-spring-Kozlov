package ru.otus.homevault.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.homevault.auth.service.RefreshTokenService;
import ru.otus.homevault.common.security.BlockedUserException;
import ru.otus.homevault.users.dto.ChangePasswordRequest;
import ru.otus.homevault.users.dto.UpdateCurrentUserRequest;
import ru.otus.homevault.users.dto.UserResponse;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.repository.UserRepository;

import java.util.UUID;

@Service
public class UsersService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final UserMapper userMapper;

    public UsersService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = findActiveUser(userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(UUID userId, UpdateCurrentUserRequest request) {
        User user = findActiveUser(userId);
        user.setDisplayName(request.displayName().trim());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findActiveUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is invalid");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenService.revokeAllForUser(userId);
    }

    private User findActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.isBlocked()) {
            throw new BlockedUserException();
        }
        return user;
    }
}

