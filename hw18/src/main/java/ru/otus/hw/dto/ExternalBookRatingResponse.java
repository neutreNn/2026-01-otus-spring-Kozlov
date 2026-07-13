package ru.otus.hw.dto;

import java.math.BigDecimal;

public record ExternalBookRatingResponse(
        String isbn,
        BigDecimal score,
        int reviewsCount) {

    public BookRatingDto toDto() {
        return new BookRatingDto(isbn, score, reviewsCount, RatingSource.EXTERNAL);
    }
}
