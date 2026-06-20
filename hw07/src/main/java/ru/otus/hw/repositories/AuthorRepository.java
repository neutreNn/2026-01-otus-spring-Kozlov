package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.models.Author;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Override
    @Query("""
            select a
            from Author a
            order by a.id
            """)
    List<Author> findAll();
}
