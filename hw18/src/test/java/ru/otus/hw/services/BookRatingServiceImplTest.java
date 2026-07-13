package ru.otus.hw.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.otus.hw.dto.RatingSource;

@DisplayName("Сервис получения рейтинга книги")
@SpringBootTest(properties = {
        "resilience4j.circuitbreaker.instances.bookRating.minimum-number-of-calls=100"
})
class BookRatingServiceImplTest {

    private static final AtomicInteger REQUESTS_COUNT = new AtomicInteger();

    private static HttpServer httpServer;

    private static volatile int responseStatus = 200;

    private static volatile String responseBody = """
            {"isbn":"9780132350884","score":4.7,"reviewsCount":128}
            """;

    @Autowired
    private BookRatingService bookRatingService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        startHttpServer();
        registry.add(
                "external.rating-service.base-url",
                () -> "http://localhost:" + httpServer.getAddress().getPort());
    }

    @AfterAll
    static void stopHttpServer() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @BeforeEach
    void setUp() {
        REQUESTS_COUNT.set(0);
        responseStatus = 200;
        responseBody = """
                {"isbn":"9780132350884","score":4.7,"reviewsCount":128}
                """;
    }

    @DisplayName("должен возвращать ответ внешнего сервиса при успешном вызове")
    @Test
    void shouldReturnExternalRating() {
        var rating = bookRatingService.findByIsbn("9780132350884");

        assertThat(rating.isbn()).isEqualTo("9780132350884");
        assertThat(rating.score()).isEqualByComparingTo(new BigDecimal("4.7"));
        assertThat(rating.reviewsCount()).isEqualTo(128);
        assertThat(rating.source()).isEqualTo(RatingSource.EXTERNAL);
        assertThat(REQUESTS_COUNT).hasValue(1);
    }

    @DisplayName("должен выполнять fallback после ошибок внешнего сервиса")
    @Test
    void shouldReturnFallbackAfterExternalServiceFailure() {
        responseStatus = 500;
        responseBody = """
                {"message":"rating service is temporarily unavailable"}
                """;

        var rating = bookRatingService.findByIsbn("9780132350884");

        assertThat(rating.isbn()).isEqualTo("9780132350884");
        assertThat(rating.score()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(rating.reviewsCount()).isZero();
        assertThat(rating.source()).isEqualTo(RatingSource.FALLBACK);
        assertThat(REQUESTS_COUNT).hasValue(2);
    }

    private static void startHttpServer() {
        if (httpServer != null) {
            return;
        }
        try {
            httpServer = HttpServer.create(new InetSocketAddress(0), 0);
            httpServer.createContext("/api/ratings/9780132350884", BookRatingServiceImplTest::handleRequest);
            httpServer.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start rating service stub", exception);
        }
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        REQUESTS_COUNT.incrementAndGet();
        var responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus, responseBytes.length);
        try (var responseBodyStream = exchange.getResponseBody()) {
            responseBodyStream.write(responseBytes);
        }
    }
}
