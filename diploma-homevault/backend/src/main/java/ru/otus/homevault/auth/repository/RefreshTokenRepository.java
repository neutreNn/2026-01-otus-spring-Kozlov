package ru.otus.homevault.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.homevault.auth.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken refreshToken
               set refreshToken.revokedAt = :revokedAt
             where refreshToken.user.id = :userId
               and refreshToken.revokedAt is null
            """)
    int revokeActiveTokensByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);
}

