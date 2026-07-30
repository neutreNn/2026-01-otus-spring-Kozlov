package ru.otus.homevault.users.service;

import org.springframework.stereotype.Component;
import ru.otus.homevault.users.dto.UserResponse;
import ru.otus.homevault.users.model.User;

import java.util.Set;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                Set.copyOf(user.getRoles()),
                user.getStorageLimitBytes(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

