package ru.otus.homevault.notes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.homevault.notes.model.Note;

import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    Optional<Note> findByIdAndOwner_Id(UUID id, UUID ownerId);

    Page<Note> findByOwner_Id(UUID ownerId, Pageable pageable);

    @Query(
            value = """
                    select distinct note
                    from Note note
                    where note.owner.id = :ownerId
                      and (
                        lower(note.title) like concat('%', lower(:query), '%')
                        or lower(note.content) like concat('%', lower(:query), '%')
                      )
                    """,
            countQuery = """
                    select count(distinct note)
                    from Note note
                    where note.owner.id = :ownerId
                      and (
                        lower(note.title) like concat('%', lower(:query), '%')
                        or lower(note.content) like concat('%', lower(:query), '%')
                      )
                    """
    )
    Page<Note> searchByQuery(
            @Param("ownerId") UUID ownerId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query(
            value = """
                    select distinct note
                    from Note note
                    join note.tags tag
                    where note.owner.id = :ownerId
                      and tag = :tag
                    """,
            countQuery = """
                    select count(distinct note)
                    from Note note
                    join note.tags tag
                    where note.owner.id = :ownerId
                      and tag = :tag
                    """
    )
    Page<Note> searchByTag(
            @Param("ownerId") UUID ownerId,
            @Param("tag") String tag,
            Pageable pageable
    );

    @Query(
            value = """
                    select distinct note
                    from Note note
                    join note.tags tag
                    where note.owner.id = :ownerId
                      and (
                        lower(note.title) like concat('%', lower(:query), '%')
                        or lower(note.content) like concat('%', lower(:query), '%')
                      )
                      and tag = :tag
                    """,
            countQuery = """
                    select count(distinct note)
                    from Note note
                    join note.tags tag
                    where note.owner.id = :ownerId
                      and (
                        lower(note.title) like concat('%', lower(:query), '%')
                        or lower(note.content) like concat('%', lower(:query), '%')
                      )
                      and tag = :tag
                    """
    )
    Page<Note> searchByQueryAndTag(
            @Param("ownerId") UUID ownerId,
            @Param("query") String query,
            @Param("tag") String tag,
            Pageable pageable
    );
}
