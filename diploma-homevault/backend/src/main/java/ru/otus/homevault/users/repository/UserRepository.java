package ru.otus.homevault.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.homevault.users.model.User;
import ru.otus.homevault.users.model.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);
}
