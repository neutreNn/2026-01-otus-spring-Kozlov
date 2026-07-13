package ru.otus.hw.migration.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.migration.row.GenreRow;
import ru.otus.hw.models.mongo.GenreDocument;

@Component
public class GenreRowToDocumentProcessor implements ItemProcessor<GenreRow, GenreDocument> {
    @Override
    public GenreDocument process(GenreRow item) {
        return new GenreDocument(item.id(), item.name());
    }
}
