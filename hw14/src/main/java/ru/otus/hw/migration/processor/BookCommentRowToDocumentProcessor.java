package ru.otus.hw.migration.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.migration.row.BookCommentRow;
import ru.otus.hw.models.mongo.BookCommentDocument;

@Component
public class BookCommentRowToDocumentProcessor implements ItemProcessor<BookCommentRow, BookCommentDocument> {
    @Override
    public BookCommentDocument process(BookCommentRow item) {
        return new BookCommentDocument(item.id(), item.text(), item.bookId());
    }
}
