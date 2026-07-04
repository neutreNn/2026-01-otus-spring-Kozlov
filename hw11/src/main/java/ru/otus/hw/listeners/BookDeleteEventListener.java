package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookCommentRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookDeleteEventListener extends AbstractMongoEventListener<Book> {
    private static final String ID_FIELD = "_id";

    private final BookCommentRepository bookCommentRepository;

    @Override
    public void onAfterDelete(AfterDeleteEvent<Book> event) {
        extractBookId(event.getSource())
                .ifPresent(this::deleteCommentsByBookId);
    }

    private void deleteCommentsByBookId(Long bookId) {
        bookCommentRepository.deleteByBookId(bookId)
                .doOnError(error -> log.warn("Failed to delete comments for book with id {}", bookId, error))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }

    private Optional<Long> extractBookId(Document source) {
        var id = source.get(ID_FIELD);
        if (id instanceof Number number) {
            return Optional.of(number.longValue());
        }
        if (id instanceof String stringId) {
            return parseLong(stringId);
        }
        return Optional.empty();
    }

    private Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
