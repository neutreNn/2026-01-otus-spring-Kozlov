package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookCreateDto {
    @NotBlank(message = "Введите название книги")
    private String title;

    @NotNull(message = "Выберите автора")
    @Positive(message = "Выберите автора")
    private Long authorId;

    @NotNull(message = "Выберите жанр")
    @Positive(message = "Выберите жанр")
    private Long genreId;

    public String normalizedTitle() {
        return title == null ? "" : title.trim();
    }
}
