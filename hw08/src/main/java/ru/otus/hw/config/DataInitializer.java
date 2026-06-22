package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.SequenceService;

@RequiredArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    private final SequenceService sequenceService;

    @Override
    public void run(ApplicationArguments args) {
        if (authorRepository.count() > 0 || genreRepository.count() > 0 || bookRepository.count() > 0) {
            return;
        }

        createLibraryData();
    }

    private void createLibraryData() {
        var author1 = authorRepository.save(new Author(sequenceService.getNextSequence("authors"), "Author_1"));
        var author2 = authorRepository.save(new Author(sequenceService.getNextSequence("authors"), "Author_2"));
        var author3 = authorRepository.save(new Author(sequenceService.getNextSequence("authors"), "Author_3"));

        var genre1 = genreRepository.save(new Genre(sequenceService.getNextSequence("genres"), "Genre_1"));
        var genre2 = genreRepository.save(new Genre(sequenceService.getNextSequence("genres"), "Genre_2"));
        var genre3 = genreRepository.save(new Genre(sequenceService.getNextSequence("genres"), "Genre_3"));

        var book1 = bookRepository.save(new Book(sequenceService.getNextSequence("books"),
                "BookTitle_1", author1, genre1));
        var book2 = bookRepository.save(new Book(sequenceService.getNextSequence("books"),
                "BookTitle_2", author2, genre2));
        bookRepository.save(new Book(sequenceService.getNextSequence("books"),
                "BookTitle_3", author3, genre3));

        bookCommentRepository.save(new BookComment(sequenceService.getNextSequence("book_comments"),
                "Comment_1", book1.getId()));
        bookCommentRepository.save(new BookComment(sequenceService.getNextSequence("book_comments"),
                "Comment_2", book1.getId()));
        bookCommentRepository.save(new BookComment(sequenceService.getNextSequence("book_comments"),
                "Comment_3", book2.getId()));
    }
}
