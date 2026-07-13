package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BookDto {
    private final Long id;

    private final String title;

    private final Long authorId;

    private final String authorFullName;

    private final Long genreId;

    private final String genreName;
}
