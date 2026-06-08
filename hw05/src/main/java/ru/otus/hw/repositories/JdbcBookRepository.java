package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JdbcBookRepository implements BookRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        var books = jdbc.query("""
                select b.id as book_id, b.title as book_title,
                       a.id as author_id, a.full_name as author_full_name,
                       g.id as genre_id, g.name as genre_name
                from books b
                         join authors a on a.id = b.author_id
                         join genres g on g.id = b.genre_id
                where b.id = :id
                """, Map.of("id", id), new BookRowMapper());
        return books.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        return jdbc.query("""
                select b.id as book_id, b.title as book_title,
                       a.id as author_id, a.full_name as author_full_name,
                       g.id as genre_id, g.name as genre_name
                from books b
                         join authors a on a.id = b.author_id
                         join genres g on g.id = b.genre_id
                order by b.id
                """, Map.of(), new BookRowMapper());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        jdbc.update("""
                delete from books
                where id = :id
                """, Map.of("id", id));
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = createBookParams(book);

        jdbc.update("""
                insert into books(title, author_id, genre_id)
                values (:title, :authorId, :genreId)
                """, params, keyHolder, new String[]{"id"});

        book.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return book;
    }

    private Book update(Book book) {
        var params = createBookParams(book)
                .addValue("id", book.getId());

        var updatedRows = jdbc.update("""
                update books
                set title = :title,
                    author_id = :authorId,
                    genre_id = :genreId
                where id = :id
                """, params);
        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }
        return book;
    }

    private MapSqlParameterSource createBookParams(Book book) {
        return new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId())
                .addValue("genreId", book.getGenre().getId());
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var author = new Author(rs.getLong("author_id"), rs.getString("author_full_name"));
            var genre = new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));
            return new Book(rs.getLong("book_id"), rs.getString("book_title"), author, genre);
        }
    }
}
