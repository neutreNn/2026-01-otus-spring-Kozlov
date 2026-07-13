package ru.otus.hw.services;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.BookRatingDto;
import ru.otus.hw.gateways.ExternalBookRatingGateway;

@Service
@RequiredArgsConstructor
public class BookRatingServiceImpl implements BookRatingService {

    private final ExternalBookRatingGateway externalBookRatingGateway;

    @Override
    @Retry(name = "bookRating", fallbackMethod = "fallback")
    @CircuitBreaker(name = "bookRating")
    @Bulkhead(name = "bookRating")
    public BookRatingDto findByIsbn(String isbn) {
        return externalBookRatingGateway.findByIsbn(isbn).toDto();
    }

    public BookRatingDto fallback(String isbn, Throwable exception) {
        return BookRatingDto.fallback(isbn);
    }
}
