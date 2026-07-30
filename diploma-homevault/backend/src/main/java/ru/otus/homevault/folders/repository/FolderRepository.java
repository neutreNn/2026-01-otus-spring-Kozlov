package ru.otus.homevault.folders.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.homevault.folders.model.Folder;

import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, UUID> {

    Page<Folder> findByOwner_IdAndParent_Id(UUID ownerId, UUID parentId, Pageable pageable);

    Page<Folder> findByOwner_IdAndParentIsNull(UUID ownerId, Pageable pageable);

    Optional<Folder> findByIdAndOwner_Id(UUID id, UUID ownerId);

    boolean existsByOwner_IdAndParent_IdAndNameIgnoreCase(UUID ownerId, UUID parentId, String name);

    boolean existsByOwner_IdAndParentIsNullAndNameIgnoreCase(UUID ownerId, String name);

    boolean existsByOwner_IdAndParent_IdAndNameIgnoreCaseAndIdNot(UUID ownerId, UUID parentId, String name, UUID id);

    boolean existsByOwner_IdAndParentIsNullAndNameIgnoreCaseAndIdNot(UUID ownerId, String name, UUID id);

    boolean existsByParent_Id(UUID parentId);
}
