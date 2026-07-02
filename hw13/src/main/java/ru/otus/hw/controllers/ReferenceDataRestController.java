package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ReferenceDataRestController {
    private final AuthorService authorService;

    private final GenreService genreService;

    @GetMapping("/authors")
    public List<Author> findAllAuthors() {
        return authorService.findAll();
    }

    @GetMapping("/genres")
    public List<Genre> findAllGenres() {
        return genreService.findAll();
    }
}
