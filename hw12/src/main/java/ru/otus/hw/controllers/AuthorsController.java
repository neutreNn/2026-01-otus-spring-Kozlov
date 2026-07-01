package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.otus.hw.services.AuthorService;

@RequiredArgsConstructor
@Controller
public class AuthorsController {
    private final AuthorService authorService;

    @GetMapping("/authors")
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorService.findAll());
        return "authors/list";
    }
}
