package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookRestController {
    private final BookService bookService;

    @GetMapping
    public List<BookDto> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable long id) {
        return bookService.findById(new BookIdDto(id));
    }

    @PostMapping
    public ResponseEntity<BookDto> create(@Valid @RequestBody BookCreateDto bookCreateDto) {
        var savedBook = bookService.insert(bookCreateDto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedBook);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable long id, @Valid @RequestBody BookCreateDto bookCreateDto) {
        return bookService.update(new BookUpdateDto(
                id,
                bookCreateDto.getTitle(),
                bookCreateDto.getAuthorId(),
                bookCreateDto.getGenreId()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        bookService.deleteById(new BookIdDto(id));
        return ResponseEntity.noContent().build();
    }
}
