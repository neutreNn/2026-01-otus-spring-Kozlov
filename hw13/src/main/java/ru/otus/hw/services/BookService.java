package ru.otus.hw.services;

import org.springframework.security.access.prepost.PreAuthorize;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;

import java.util.List;

public interface BookService {
    @PreAuthorize("isAuthenticated()")
    BookDto findById(BookIdDto bookIdDto);

    @PreAuthorize("isAuthenticated()")
    List<BookDto> findAll();

    @PreAuthorize("@bookAuthorizationService.canCreate(authentication, #bookCreateDto)")
    BookDto insert(BookCreateDto bookCreateDto);

    @PreAuthorize("@bookAuthorizationService.canUpdate(authentication, #bookUpdateDto)")
    BookDto update(BookUpdateDto bookUpdateDto);

    @PreAuthorize("@bookAuthorizationService.canDelete(authentication, #bookIdDto)")
    void deleteById(BookIdDto bookIdDto);
}
