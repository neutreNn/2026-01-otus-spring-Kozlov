package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.otus.hw.models.Book;

@Getter
@Setter
@NoArgsConstructor
public class BookForm {
    private Long id;

    @NotBlank(message = "Введите название книги")
    private String title;

    @Positive(message = "Выберите автора")
    private long authorId;

    @Positive(message = "Выберите жанр")
    private long genreId;

    public BookForm(Long id, String title, long authorId, long genreId) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.genreId = genreId;
    }

    public static BookForm from(Book book) {
        return new BookForm(book.getId(), book.getTitle(), book.getAuthor().getId(), book.getGenre().getId());
    }

    public String normalizedTitle() {
        return title == null ? "" : title.trim();
    }
}
