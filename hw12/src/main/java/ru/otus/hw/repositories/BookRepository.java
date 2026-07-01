package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Override
    @EntityGraph(value = Book.AUTHOR_GENRE_ENTITY_GRAPH, type = EntityGraph.EntityGraphType.FETCH)
    Optional<Book> findById(Long id);

    @Override
    @EntityGraph(value = Book.AUTHOR_GENRE_ENTITY_GRAPH, type = EntityGraph.EntityGraphType.FETCH)
    @Query("""
            select b
            from Book b
            order by b.id
            """)
    List<Book> findAll();
}
