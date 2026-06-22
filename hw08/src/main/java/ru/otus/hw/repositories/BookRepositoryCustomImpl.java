package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {
    private final MongoOperations mongoOperations;

    @Override
    public void deleteByIdWithComments(long id) {
        var bookQuery = Query.query(Criteria.where("_id").is(id));
        var commentQuery = Query.query(Criteria.where("book_id").is(id));

        mongoOperations.remove(commentQuery, BookComment.class);
        mongoOperations.remove(bookQuery, Book.class);
    }
}
