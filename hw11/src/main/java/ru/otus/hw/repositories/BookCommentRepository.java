package ru.otus.hw.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookComment;

public interface BookCommentRepository extends ReactiveMongoRepository<BookComment, Long> {
    Flux<BookComment> findByBookId(Long bookId, Sort sort);

    Mono<Long> deleteByBookId(Long bookId);
}
