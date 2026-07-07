package ru.otus.hw.migration.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.migration.row.AuthorRow;
import ru.otus.hw.models.mongo.AuthorDocument;

@Component
public class AuthorRowToDocumentProcessor implements ItemProcessor<AuthorRow, AuthorDocument> {
    @Override
    public AuthorDocument process(AuthorRow item) {
        return new AuthorDocument(item.id(), item.fullName());
    }
}
