package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books/{bookId}/comments")
public class BookCommentRestController {
    private final BookService bookService;

    private final BookCommentService bookCommentService;

    @GetMapping
    public List<BookCommentDto> findByBookId(@PathVariable long bookId) {
        bookService.findById(new BookIdDto(bookId));
        return bookCommentService.findByBookId(bookId).stream()
                .map(BookCommentDto::from)
                .toList();
    }
}
