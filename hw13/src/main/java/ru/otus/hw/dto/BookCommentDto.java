package ru.otus.hw.dto;

import ru.otus.hw.models.BookComment;

public record BookCommentDto(long id, String text) {
    public static BookCommentDto from(BookComment comment) {
        return new BookCommentDto(comment.getId(), comment.getText());
    }
}
