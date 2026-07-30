package ru.otus.homevault.sharing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.homevault.sharing.model.ShareLink;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {

    Page<ShareLink> findByOwner_Id(UUID ownerId, Pageable pageable);

    Optional<ShareLink> findByIdAndOwner_Id(UUID id, UUID ownerId);

    Optional<ShareLink> findByToken(String token);

    boolean existsByToken(String token);

    @Query("select count(share) from ShareLink share where share.revokedAt is null and share.expiresAt > :now")
    long countActive(@Param("now") Instant now);
}
