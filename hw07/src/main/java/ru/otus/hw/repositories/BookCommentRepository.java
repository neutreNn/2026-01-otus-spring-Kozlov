package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.otus.hw.models.BookComment;

import java.util.List;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
    @Query("""
            select c
            from BookComment c
            where c.book.id = :bookId
            order by c.id
            """)
    List<BookComment> findByBookId(@Param("bookId") long bookId);
}
