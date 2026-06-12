package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.services.BookCommentService;

import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommentCommands {
    private final BookCommentService bookCommentService;

    private final BookCommentConverter bookCommentConverter;

    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(long id) {
        return bookCommentService.findById(id)
                .map(bookCommentConverter::commentToString)
                .orElse("Book comment with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find comments by book id", key = "cabid")
    public String findCommentsByBookId(long bookId) {
        return bookCommentService.findByBookId(bookId).stream()
                .map(bookCommentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert comment", key = "cins")
    public String insertComment(String text, long bookId) {
        var savedComment = bookCommentService.insert(text, bookId);
        return bookCommentConverter.commentToString(savedComment);
    }

    @ShellMethod(value = "Update comment", key = "cupd")
    public String updateComment(long id, String text, long bookId) {
        var savedComment = bookCommentService.update(id, text, bookId);
        return bookCommentConverter.commentToString(savedComment);
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(long id) {
        bookCommentService.deleteById(id);
    }
}
