package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
    List<BookComment> findByBookId(long bookId, Sort sort);
}
