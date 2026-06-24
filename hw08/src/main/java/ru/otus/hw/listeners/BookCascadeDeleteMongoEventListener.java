package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BookCascadeDeleteMongoEventListener extends AbstractMongoEventListener<Book> {
    private final MongoOperations mongoOperations;

    @Override
    public void onAfterDelete(AfterDeleteEvent<Book> event) {
        extractBookId(event).ifPresent(bookId -> {
            var commentQuery = Query.query(Criteria.where("book_id").is(bookId));
            mongoOperations.remove(commentQuery, BookComment.class);
        });
    }

    private Optional<Long> extractBookId(AfterDeleteEvent<Book> event) {
        var id = event.getSource().get("_id");

        if (id instanceof Number number) {
            return Optional.of(number.longValue());
        }
        if (id instanceof String idValue) {
            return Optional.of(Long.parseLong(idValue));
        }
        return Optional.empty();
    }
}
