package ru.otus.hw.migration.row;

public record BookRow(
        long id,
        String title,
        long authorId,
        String authorFullName,
        long genreId,
        String genreName) {
}
