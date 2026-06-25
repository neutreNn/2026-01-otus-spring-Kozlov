package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.models.Book;
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
        var book = findBook(id);
        model.addAttribute("book", book);
        model.addAttribute("comments", bookCommentService.findByBookId(id));
        return "books/details";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("formAction", "/books");
        addReferenceData(model);
        return "books/form";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute("bookForm") BookForm bookForm,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/books");
            addReferenceData(model);
            return "books/form";
        }

        var savedBook = bookService.insert(bookForm.normalizedTitle(), bookForm.getAuthorId(), bookForm.getGenreId());
        return "redirect:/books/%d".formatted(savedBook.getId());
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable long id, Model model) {
        var book = findBook(id);
        model.addAttribute("bookForm", BookForm.from(book));
        model.addAttribute("formAction", "/books/%d".formatted(id));
        addReferenceData(model);
        return "books/form";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable long id,
                             @Valid @ModelAttribute("bookForm") BookForm bookForm,
                             BindingResult bindingResult,
                             Model model) {
        bookForm.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/books/%d".formatted(id));
            addReferenceData(model);
            return "books/form";
        }

        bookService.update(id, bookForm.normalizedTitle(), bookForm.getAuthorId(), bookForm.getGenreId());
        return "redirect:/books/%d".formatted(id);
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private Book findBook(long id) {
        return bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Book with id %d not found".formatted(id)));
    }

    private void addReferenceData(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
    }
}
