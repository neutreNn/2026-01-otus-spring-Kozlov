package ru.otus.hw.services;

import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;

import java.util.List;

public interface BookService {
    BookDto findById(BookIdDto bookIdDto);

    List<BookDto> findAll();

    BookDto insert(BookCreateDto bookCreateDto);

    BookDto update(BookUpdateDto bookUpdateDto);

    void deleteById(BookIdDto bookIdDto);
}
