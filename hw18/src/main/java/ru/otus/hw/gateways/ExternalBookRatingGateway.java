package ru.otus.hw.gateways;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.otus.hw.dto.ExternalBookRatingResponse;

@Component
@RequiredArgsConstructor
public class ExternalBookRatingGateway {

    private final RestClient bookRatingRestClient;

    public ExternalBookRatingResponse findByIsbn(String isbn) {
        return bookRatingRestClient.get()
                .uri("/api/ratings/{isbn}", isbn)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ExternalBookRatingResponse.class);
    }
}
