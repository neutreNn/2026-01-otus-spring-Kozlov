package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        var books = entityManager.createQuery("""
                        select b
                        from Book b
                            join fetch b.author
                            join fetch b.genre
                        where b.id = :id
                        """, Book.class)
                .setParameter("id", id)
                .getResultList();

        return books.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery("""
                        select b
                        from Book b
                            join fetch b.author
                            join fetch b.genre
                        order by b.id
                        """, Book.class)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
        }

        var persistedBook = entityManager.find(Book.class, book.getId());
        if (persistedBook == null) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }
        persistedBook.setTitle(book.getTitle());
        persistedBook.setAuthor(book.getAuthor());
        persistedBook.setGenre(book.getGenre());
        return persistedBook;
    }

    @Override
    public void deleteById(long id) {
        entityManager.createQuery("""
                        delete from Book b
                        where b.id = :id
                        """)
                .setParameter("id", id)
                .executeUpdate();
    }
}
