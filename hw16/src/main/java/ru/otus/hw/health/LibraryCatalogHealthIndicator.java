package ru.otus.hw.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@Component
@RequiredArgsConstructor
public class LibraryCatalogHealthIndicator implements HealthIndicator {
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Health health() {
        try {
            var booksCount = bookRepository.count();
            var authorsCount = authorRepository.count();
            var genresCount = genreRepository.count();
            var healthBuilder = isCatalogReady(booksCount, authorsCount, genresCount) ? Health.up() : Health.down();

            return healthBuilder
                    .withDetail("books", booksCount)
                    .withDetail("authors", authorsCount)
                    .withDetail("genres", genresCount)
                    .build();
        } catch (RuntimeException exception) {
            return Health.down(exception).build();
        }
    }

    private boolean isCatalogReady(long booksCount, long authorsCount, long genresCount) {
        return booksCount > 0 && authorsCount > 0 && genresCount > 0;
    }
}
