package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import ru.otus.hw.listeners.BookCascadeDeleteMongoEventListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.DatabaseSequence;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@DataMongoTest
@Import({
        AuthorServiceImpl.class,
        BookCommentServiceImpl.class,
        BookServiceImpl.class,
        GenreServiceImpl.class,
        BookCascadeDeleteMongoEventListener.class,
        SequenceService.class
})
abstract class ServiceMongoTestBase {
    @Autowired
    protected AuthorRepository authorRepository;

    @Autowired
    protected GenreRepository genreRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected BookCommentRepository bookCommentRepository;

    @Autowired
    private MongoOperations mongoOperations;

    @BeforeEach
    void setUp() {
        mongoOperations.dropCollection(Author.class);
        mongoOperations.dropCollection(Genre.class);
        mongoOperations.dropCollection(Book.class);
        mongoOperations.dropCollection(BookComment.class);
        mongoOperations.dropCollection(DatabaseSequence.class);

        var author1 = authorRepository.save(new Author(1L, "Author_1"));
        var author2 = authorRepository.save(new Author(2L, "Author_2"));
        var author3 = authorRepository.save(new Author(3L, "Author_3"));

        var genre1 = genreRepository.save(new Genre(1L, "Genre_1"));
        var genre2 = genreRepository.save(new Genre(2L, "Genre_2"));
        var genre3 = genreRepository.save(new Genre(3L, "Genre_3"));

        bookRepository.save(new Book(1L, "BookTitle_1", author1, genre1));
        bookRepository.save(new Book(2L, "BookTitle_2", author2, genre2));
        bookRepository.save(new Book(3L, "BookTitle_3", author3, genre3));

        bookCommentRepository.save(new BookComment(1L, "Comment_1", 1L));
        bookCommentRepository.save(new BookComment(2L, "Comment_2", 1L));
        bookCommentRepository.save(new BookComment(3L, "Comment_3", 2L));

        mongoOperations.save(new DatabaseSequence("authors", 3));
        mongoOperations.save(new DatabaseSequence("genres", 3));
        mongoOperations.save(new DatabaseSequence("books", 3));
        mongoOperations.save(new DatabaseSequence("book_comments", 3));
    }
}
