package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.BookComment;

@RequiredArgsConstructor
@Component
public class BookCommentConverter {
    private final BookConverter bookConverter;

    public String commentToString(BookComment comment) {
        return "Id: %d, text: %s, book: {%s}".formatted(
                comment.getId(),
                comment.getText(),
                bookConverter.bookToString(comment.getBook()));
    }
}
