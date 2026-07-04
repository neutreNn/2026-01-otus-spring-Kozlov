package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.BookComment;

public interface BookCommentService {
    Mono<BookComment> findById(long id);

    Flux<BookComment> findByBookId(long bookId);

    Mono<BookComment> insert(String text, long bookId);

    Mono<BookComment> update(long id, String text);

    Mono<Void> deleteById(long id);
}
