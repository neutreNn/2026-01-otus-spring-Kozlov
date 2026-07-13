package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.mongo.BookCommentDocument;

public interface BookCommentDocumentRepository extends MongoRepository<BookCommentDocument, Long> {
}
