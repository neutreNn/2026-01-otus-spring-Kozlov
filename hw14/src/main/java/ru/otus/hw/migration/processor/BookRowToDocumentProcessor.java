package ru.otus.hw.migration.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.migration.row.BookRow;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.GenreDocument;

@Component
public class BookRowToDocumentProcessor implements ItemProcessor<BookRow, BookDocument> {
    @Override
    public BookDocument process(BookRow item) {
        var author = new AuthorDocument(item.authorId(), item.authorFullName());
        var genre = new GenreDocument(item.genreId(), item.genreName());

        return new BookDocument(item.id(), item.title(), author, genre);
    }
}
