package ru.otus.hw.services;

import ru.otus.hw.dto.BookRatingDto;

public interface BookRatingService {

    BookRatingDto findByIsbn(String isbn);
}
