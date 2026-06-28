package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public BookDto findById(BookIdDto bookIdDto) {
        return bookRepository.findById(bookIdDto.getId())
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book with id %d not found".formatted(bookIdDto.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public BookDto insert(BookCreateDto bookCreateDto) {
        var authorId = bookCreateDto.getAuthorId();
        var genreId = bookCreateDto.getGenreId();
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %d not found".formatted(genreId)));
        var book = new Book(0, bookCreateDto.normalizedTitle(), author, genre);
        return toDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDto update(BookUpdateDto bookUpdateDto) {
        var bookId = bookUpdateDto.getId();
        var authorId = bookUpdateDto.getAuthorId();
        var genreId = bookUpdateDto.getGenreId();
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %d not found".formatted(genreId)));

        book.setTitle(bookUpdateDto.normalizedTitle());
        book.setAuthor(author);
        book.setGenre(genre);
        return toDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteById(BookIdDto bookIdDto) {
        bookRepository.deleteById(bookIdDto.getId());
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
