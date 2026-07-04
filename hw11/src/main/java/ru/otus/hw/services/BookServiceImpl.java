package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private static final String BOOKS_SEQUENCE = "books";

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final SequenceService sequenceService;

    @Override
    public Mono<BookDto> findById(BookIdDto bookIdDto) {
        return bookRepository.findById(bookIdDto.getId())
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookIdDto.getId()))))
                .map(this::toDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllByOrderByIdAsc()
                .map(this::toDto);
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto bookCreateDto) {
        var authorId = bookCreateDto.getAuthorId();
        var genreId = bookCreateDto.getGenreId();
        return Mono.zip(findAuthor(authorId), findGenre(genreId))
                .flatMap(tuple -> sequenceService.getNextSequence(BOOKS_SEQUENCE)
                        .map(id -> new Book(id, bookCreateDto.normalizedTitle(), tuple.getT1(), tuple.getT2())))
                .flatMap(bookRepository::save)
                .map(this::toDto);
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        var bookId = bookUpdateDto.getId();
        var authorId = bookUpdateDto.getAuthorId();
        var genreId = bookUpdateDto.getGenreId();
        return Mono.zip(findBook(bookId), findAuthor(authorId), findGenre(genreId))
                .map(tuple -> {
                    var book = tuple.getT1();
                    book.setTitle(bookUpdateDto.normalizedTitle());
                    book.setAuthor(tuple.getT2());
                    book.setGenre(tuple.getT3());
                    return book;
                })
                .flatMap(bookRepository::save)
                .map(this::toDto);
    }

    @Override
    public Mono<Void> deleteById(BookIdDto bookIdDto) {
        return bookRepository.deleteById(bookIdDto.getId());
    }

    private Mono<Book> findBook(Long bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookId))));
    }

    private Mono<Author> findAuthor(Long authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Author with id %d not found".formatted(authorId))));
    }

    private Mono<Genre> findGenre(Long genreId) {
        return genreRepository.findById(genreId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Genre with id %d not found".formatted(genreId))));
    }

    private BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getAuthor().getFullName(),
                book.getGenre().getId(),
                book.getGenre().getName()
        );
    }
}
