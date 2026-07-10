package ru.otus.hw.dto;

import java.math.BigDecimal;

public record BookRatingDto(
        String isbn,
        BigDecimal score,
        int reviewsCount,
        RatingSource source) {

    public static BookRatingDto fallback(String isbn) {
        return new BookRatingDto(isbn, BigDecimal.ZERO, 0, RatingSource.FALLBACK);
    }
}
