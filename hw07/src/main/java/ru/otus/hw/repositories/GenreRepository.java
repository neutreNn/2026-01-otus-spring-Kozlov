package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.models.Genre;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    @Override
    @Query("""
            select g
            from Genre g
            order by g.id
            """)
    List<Genre> findAll();
}
