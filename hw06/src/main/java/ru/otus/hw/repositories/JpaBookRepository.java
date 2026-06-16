package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {
    private static final String FETCH_GRAPH_HINT = "jakarta.persistence.fetchgraph";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        var properties = Map.<String, Object>of(
                FETCH_GRAPH_HINT,
                entityManager.getEntityGraph(Book.AUTHOR_GENRE_ENTITY_GRAPH));
        return Optional.ofNullable(entityManager.find(Book.class, id, properties));
    }

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery("""
                        select b
                        from Book b
                        order by b.id
                        """, Book.class)
                .setHint(FETCH_GRAPH_HINT, entityManager.getEntityGraph(Book.AUTHOR_GENRE_ENTITY_GRAPH))
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
        }

        return entityManager.merge(book);
    }

    @Override
    public void deleteById(long id) {
        var book = entityManager.find(Book.class, id);
        if (book != null) {
            entityManager.remove(book);
        }
    }
}
