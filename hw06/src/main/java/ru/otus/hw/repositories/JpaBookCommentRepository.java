package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookCommentRepository implements BookCommentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<BookComment> findById(long id) {
        var comments = entityManager.createQuery("""
                        select c
                        from BookComment c
                            join fetch c.book b
                            join fetch b.author
                            join fetch b.genre
                        where c.id = :id
                        """, BookComment.class)
                .setParameter("id", id)
                .getResultList();

        return comments.stream().findFirst();
    }

    @Override
    public List<BookComment> findByBookId(long bookId) {
        return entityManager.createQuery("""
                        select c
                        from BookComment c
                            join fetch c.book b
                            join fetch b.author
                            join fetch b.genre
                        where b.id = :bookId
                        order by c.id
                        """, BookComment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public BookComment save(BookComment comment) {
        if (comment.getId() == 0) {
            entityManager.persist(comment);
            return comment;
        }

        var persistedComment = entityManager.find(BookComment.class, comment.getId());
        if (persistedComment == null) {
            throw new EntityNotFoundException("Book comment with id %d not found".formatted(comment.getId()));
        }
        persistedComment.setText(comment.getText());
        persistedComment.setBook(comment.getBook());
        return persistedComment;
    }

    @Override
    public void deleteById(long id) {
        entityManager.createQuery("""
                        delete from BookComment c
                        where c.id = :id
                        """)
                .setParameter("id", id)
                .executeUpdate();
    }
}
