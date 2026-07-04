package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {
    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks() {
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String showBook(@PathVariable long id, Model model) {
        model.addAttribute("bookId", id);
        return "books/details";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("bookId", null);
        return "books/form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable long id, Model model) {
        model.addAttribute("bookId", id);
        return "books/form";
    }
}
