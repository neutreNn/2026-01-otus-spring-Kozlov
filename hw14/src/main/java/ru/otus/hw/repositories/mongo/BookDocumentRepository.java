package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.BookDocument;

public interface BookDocumentRepository extends MongoRepository<BookDocument, Long> {
}
