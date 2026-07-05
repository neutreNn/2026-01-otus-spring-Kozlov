package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Авторизация сервисных операций с книгами")
@SpringBootTest
@Transactional
class BookServiceAuthorizationTest {
    @Autowired
    private BookService bookService;

    @DisplayName("должна запрещать чтение без аутентификации")
    @Test
    void shouldRejectReadOperationForAnonymousUser() {
        assertThatThrownBy(() -> bookService.findAll())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @DisplayName("должна разрешать чтение обычному пользователю")
    @Test
    @WithMockUser(username = "reader", roles = "USER")
    void shouldAllowReadOperationForRegularUser() {
        assertThat(bookService.findAll()).hasSize(3);
    }

    @DisplayName("должна запрещать создание книги обычному пользователю")
    @Test
    @WithMockUser(username = "reader", roles = "USER")
    void shouldRejectCreateOperationForRegularUser() {
        assertThatThrownBy(() -> bookService.insert(new BookCreateDto("Book", 1L, 1L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("должна разрешать редактору создавать книгу")
    @Test
    @WithMockUser(username = "editor", roles = "EDITOR")
    void shouldAllowEditorToCreateBook() {
        var actualBook = bookService.insert(new BookCreateDto("  Editor Book  ", 1L, 2L));

        assertThat(actualBook.getId()).isPositive();
        assertThat(actualBook.getTitle()).isEqualTo("Editor Book");
        assertThat(actualBook.getGenreId()).isEqualTo(2L);
    }

    @DisplayName("должна разрешать редактору обновлять книгу")
    @Test
    @WithMockUser(username = "editor", roles = "EDITOR")
    void shouldAllowEditorToUpdateBook() {
        var actualBook = bookService.update(new BookUpdateDto(2L, "  Updated Book  ", 1L, 3L));

        assertThat(actualBook.getId()).isEqualTo(2L);
        assertThat(actualBook.getTitle()).isEqualTo("Updated Book");
        assertThat(actualBook.getGenreId()).isEqualTo(3L);
    }

    @DisplayName("должна запрещать редактору удалять книгу")
    @Test
    @WithMockUser(username = "editor", roles = "EDITOR")
    void shouldRejectDeleteOperationForEditor() {
        assertThatThrownBy(() -> bookService.deleteById(new BookIdDto(1L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @DisplayName("должна разрешать администратору удалять книгу")
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAdminToDeleteBook() {
        bookService.deleteById(new BookIdDto(1L));

        assertThatThrownBy(() -> bookService.findById(new BookIdDto(1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
