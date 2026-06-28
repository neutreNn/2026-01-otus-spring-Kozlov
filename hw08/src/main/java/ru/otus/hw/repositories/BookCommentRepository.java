package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;

public interface BookCommentRepository extends MongoRepository<BookComment, Long> {
    List<BookComment> findByBookId(long bookId, Sort sort);

    void deleteByBookId(long bookId);
}
