package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.GenreService;

@RequiredArgsConstructor
@Controller
public class GenresController {
    private final GenreService genreService;

    @GetMapping("/genres")
    public Mono<String> listGenres(Model model) {
        return genreService.findAll()
                .collectList()
                .doOnNext(genres -> model.addAttribute("genres", genres))
                .thenReturn("genres/list");
    }
}
