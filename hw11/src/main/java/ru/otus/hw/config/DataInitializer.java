package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.SequenceService;

import java.util.List;

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
        var hasData = Mono.zip(authorRepository.count(), genreRepository.count(), bookRepository.count())
                .map(tuple -> tuple.getT1() > 0 || tuple.getT2() > 0 || tuple.getT3() > 0)
                .block();

        if (Boolean.TRUE.equals(hasData)) {
            return;
        }

        createLibraryData().block();
    }

    private Mono<Void> createLibraryData() {
        return Mono.zip(createAuthors(), createGenres())
                .flatMap(tuple -> createBooks(tuple.getT1(), tuple.getT2()))
                .flatMap(this::createComments)
                .then();
    }

    private Mono<List<Author>> createAuthors() {
        return Flux.range(1, 3)
                .concatMap(index -> sequenceService.getNextSequence("authors")
                        .map(id -> new Author(id, "Author_%d".formatted(index)))
                        .flatMap(authorRepository::save))
                .collectList();
    }

    private Mono<List<Genre>> createGenres() {
        return Flux.range(1, 3)
                .concatMap(index -> sequenceService.getNextSequence("genres")
                        .map(id -> new Genre(id, "Genre_%d".formatted(index)))
                        .flatMap(genreRepository::save))
                .collectList();
    }

    private Mono<List<Book>> createBooks(List<Author> authors, List<Genre> genres) {
        return Flux.range(1, 3)
                .concatMap(index -> sequenceService.getNextSequence("books")
                        .map(id -> new Book(
                                id,
                                "BookTitle_%d".formatted(index),
                                authors.get(index - 1),
                                genres.get(index - 1)))
                        .flatMap(bookRepository::save))
                .collectList();
    }

    private Mono<Void> createComments(List<Book> books) {
        return Flux.just(
                        new CommentSeed("Comment_1", books.get(0).getId()),
                        new CommentSeed("Comment_2", books.get(0).getId()),
                        new CommentSeed("Comment_3", books.get(1).getId()))
                .concatMap(seed -> sequenceService.getNextSequence("book_comments")
                        .map(id -> new BookComment(id, seed.text(), seed.bookId()))
                        .flatMap(bookCommentRepository::save))
                .then();
    }

    private record CommentSeed(String text, Long bookId) {
    }
}
