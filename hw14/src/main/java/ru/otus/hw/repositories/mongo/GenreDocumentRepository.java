package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.GenreDocument;

public interface GenreDocumentRepository extends MongoRepository<GenreDocument, Long> {
}
