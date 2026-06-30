package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.AuthorService;

@RequiredArgsConstructor
@Controller
public class AuthorsController {
    private final AuthorService authorService;

    @GetMapping("/authors")
    public Mono<String> listAuthors(Model model) {
        return authorService.findAll()
                .collectList()
                .doOnNext(authors -> model.addAttribute("authors", authors))
                .thenReturn("authors/list");
    }
}
