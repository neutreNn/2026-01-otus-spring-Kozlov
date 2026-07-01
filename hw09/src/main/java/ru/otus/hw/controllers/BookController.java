package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

@RequiredArgsConstructor
@Controller
public class BookController {
    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookCommentService bookCommentService;

    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String showBook(@PathVariable long id, Model model) {
        var book = bookService.findById(new BookIdDto(id));
        model.addAttribute("book", book);
        model.addAttribute("comments", bookCommentService.findByBookId(id));
        return "books/details";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("book", new BookCreateDto());
        addFormAttributes(model, null, "/books");
        addReferenceData(model);
        return "books/form";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute("book") BookCreateDto bookCreateDto,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, null, "/books");
            addReferenceData(model);
            return "books/form";
        }

        var savedBook = bookService.insert(bookCreateDto);
        return "redirect:/books/%d".formatted(savedBook.getId());
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable long id, Model model) {
        var book = bookService.findById(new BookIdDto(id));
        model.addAttribute("book", BookUpdateDto.from(book));
        addFormAttributes(model, id, "/books/%d".formatted(id));
        addReferenceData(model);
        return "books/form";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable long id,
                             @Valid @ModelAttribute("book") BookUpdateDto bookUpdateDto,
                             BindingResult bindingResult,
                             Model model) {
        bookUpdateDto.setId(id);
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, id, "/books/%d".formatted(id));
            addReferenceData(model);
            return "books/form";
        }

        bookService.update(bookUpdateDto);
        return "redirect:/books/%d".formatted(id);
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(new BookIdDto(id));
        return "redirect:/books";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
    }

    private void addFormAttributes(Model model, Long bookId, String formAction) {
        model.addAttribute("bookId", bookId);
        model.addAttribute("formAction", formAction);
    }
}
