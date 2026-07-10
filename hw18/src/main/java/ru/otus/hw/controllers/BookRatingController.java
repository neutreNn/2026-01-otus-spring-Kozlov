package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookRatingDto;
import ru.otus.hw.services.BookRatingService;

import jakarta.validation.constraints.Pattern;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookRatingController {

    private final BookRatingService bookRatingService;

    @GetMapping("/{isbn}/rating")
    public BookRatingDto getRating(@PathVariable @Pattern(regexp = "\\d{10}|\\d{13}") String isbn) {
        return bookRatingService.findByIsbn(isbn);
    }
}
