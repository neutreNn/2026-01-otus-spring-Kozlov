package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.ApplicationUser;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    Optional<ApplicationUser> findByUsername(String username);
}
