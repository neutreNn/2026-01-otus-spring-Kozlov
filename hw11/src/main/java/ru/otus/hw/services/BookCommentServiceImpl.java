package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {
    private static final String BOOK_COMMENTS_SEQUENCE = "book_comments";

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    private final SequenceService sequenceService;

    @Override
    public Mono<BookComment> findById(long id) {
        return bookCommentRepository.findById(id);
    }

    @Override
    public Flux<BookComment> findByBookId(long bookId) {
        return bookCommentRepository.findByBookId(bookId, Sort.by("id"));
    }

    @Override
    public Mono<BookComment> insert(String text, long bookId) {
        return bookRepository.existsById(bookId)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId))))
                .then(sequenceService.getNextSequence(BOOK_COMMENTS_SEQUENCE))
                .map(id -> new BookComment(id, text, bookId))
                .flatMap(bookCommentRepository::save);
    }

    @Override
    public Mono<BookComment> update(long id, String text) {
        return bookCommentRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book comment with id %d not found".formatted(id))))
                .map(comment -> {
                    comment.setText(text);
                    return comment;
                })
                .flatMap(bookCommentRepository::save);
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return bookCommentRepository.deleteById(id);
    }
}
