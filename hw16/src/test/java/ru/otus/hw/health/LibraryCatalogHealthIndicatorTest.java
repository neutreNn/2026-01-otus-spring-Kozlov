package ru.otus.hw.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HealthCheck индикатор состояния библиотечного каталога")
@DataJpaTest
@Import(LibraryCatalogHealthIndicator.class)
class LibraryCatalogHealthIndicatorTest {
    @Autowired
    private LibraryCatalogHealthIndicator healthIndicator;

    @DisplayName("должен возвращать UP, если каталог доступен и наполнен")
    @Test
    void shouldReturnUpWhenCatalogIsAvailableAndFilled() {
        var health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("books", 3L)
                .containsEntry("authors", 3L)
                .containsEntry("genres", 3L);
    }
}
