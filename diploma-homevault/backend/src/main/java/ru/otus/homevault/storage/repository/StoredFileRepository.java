package ru.otus.homevault.storage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.homevault.storage.model.StoredFile;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    Page<StoredFile> findByOwner_IdAndFolder_Id(UUID ownerId, UUID folderId, Pageable pageable);

    Page<StoredFile> findByOwner_IdAndFolderIsNull(UUID ownerId, Pageable pageable);

    Optional<StoredFile> findByIdAndOwner_Id(UUID id, UUID ownerId);

    boolean existsByFolder_Id(UUID folderId);

    @Query("select coalesce(sum(file.sizeBytes), 0) from StoredFile file")
    long sumSizeBytes();
}
