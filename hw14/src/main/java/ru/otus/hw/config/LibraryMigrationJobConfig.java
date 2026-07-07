package ru.otus.hw.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.migration.processor.AuthorRowToDocumentProcessor;
import ru.otus.hw.migration.processor.BookCommentRowToDocumentProcessor;
import ru.otus.hw.migration.processor.BookRowToDocumentProcessor;
import ru.otus.hw.migration.processor.GenreRowToDocumentProcessor;
import ru.otus.hw.migration.row.AuthorRow;
import ru.otus.hw.migration.row.BookCommentRow;
import ru.otus.hw.migration.row.BookRow;
import ru.otus.hw.migration.row.GenreRow;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookCommentDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.repositories.mongo.AuthorDocumentRepository;
import ru.otus.hw.repositories.mongo.BookCommentDocumentRepository;
import ru.otus.hw.repositories.mongo.BookDocumentRepository;
import ru.otus.hw.repositories.mongo.GenreDocumentRepository;

@Configuration
public class LibraryMigrationJobConfig {
    private static final int CHUNK_SIZE = 50;

    @Bean
    public Job libraryMigrationJob(JobRepository jobRepository,
                                   @Qualifier("migrateAuthorsStep") Step migrateAuthorsStep,
                                   @Qualifier("migrateGenresStep") Step migrateGenresStep,
                                   @Qualifier("migrateBooksStep") Step migrateBooksStep,
                                   @Qualifier("migrateBookCommentsStep") Step migrateBookCommentsStep) {
        return new JobBuilder("libraryMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(migrateAuthorsStep)
                .next(migrateGenresStep)
                .next(migrateBooksStep)
                .next(migrateBookCommentsStep)
                .build();
    }

    @Bean
    public Step migrateAuthorsStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   @Qualifier("authorReader") JdbcCursorItemReader<AuthorRow> reader,
                                   AuthorRowToDocumentProcessor processor,
                                   @Qualifier("authorWriter") ItemWriter<AuthorDocument> writer) {
        return new StepBuilder("migrateAuthorsStep", jobRepository)
                .<AuthorRow, AuthorDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step migrateGenresStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  @Qualifier("genreReader") JdbcCursorItemReader<GenreRow> reader,
                                  GenreRowToDocumentProcessor processor,
                                  @Qualifier("genreWriter") ItemWriter<GenreDocument> writer) {
        return new StepBuilder("migrateGenresStep", jobRepository)
                .<GenreRow, GenreDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step migrateBooksStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 @Qualifier("bookReader") JdbcCursorItemReader<BookRow> reader,
                                 BookRowToDocumentProcessor processor,
                                 @Qualifier("bookWriter") ItemWriter<BookDocument> writer) {
        return new StepBuilder("migrateBooksStep", jobRepository)
                .<BookRow, BookDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step migrateBookCommentsStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        @Qualifier("bookCommentReader") JdbcCursorItemReader<BookCommentRow> reader,
                                        BookCommentRowToDocumentProcessor processor,
                                        @Qualifier("bookCommentWriter") ItemWriter<BookCommentDocument> writer) {
        return new StepBuilder("migrateBookCommentsStep", jobRepository)
                .<BookCommentRow, BookCommentDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AuthorRow> authorReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<AuthorRow>()
                .name("authorReader")
                .dataSource(dataSource)
                .sql("select id, full_name from authors order by id")
                .rowMapper(this::mapAuthor)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<GenreRow> genreReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<GenreRow>()
                .name("genreReader")
                .dataSource(dataSource)
                .sql("select id, name from genres order by id")
                .rowMapper(this::mapGenre)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<BookRow> bookReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<BookRow>()
                .name("bookReader")
                .dataSource(dataSource)
                .sql("""
                        select b.id,
                               b.title,
                               a.id as author_id,
                               a.full_name as author_full_name,
                               g.id as genre_id,
                               g.name as genre_name
                        from books b
                        join authors a on a.id = b.author_id
                        join genres g on g.id = b.genre_id
                        order by b.id
                        """)
                .rowMapper(this::mapBook)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<BookCommentRow> bookCommentReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<BookCommentRow>()
                .name("bookCommentReader")
                .dataSource(dataSource)
                .sql("select id, text, book_id from book_comments order by id")
                .rowMapper(this::mapBookComment)
                .build();
    }

    @Bean
    public ItemWriter<AuthorDocument> authorWriter(AuthorDocumentRepository repository) {
        return saveAllWriter(repository);
    }

    @Bean
    public ItemWriter<GenreDocument> genreWriter(GenreDocumentRepository repository) {
        return saveAllWriter(repository);
    }

    @Bean
    public ItemWriter<BookDocument> bookWriter(BookDocumentRepository repository) {
        return saveAllWriter(repository);
    }

    @Bean
    public ItemWriter<BookCommentDocument> bookCommentWriter(BookCommentDocumentRepository repository) {
        return saveAllWriter(repository);
    }

    private AuthorRow mapAuthor(ResultSet resultSet, int rowNumber) throws SQLException {
        return new AuthorRow(resultSet.getLong("id"), resultSet.getString("full_name"));
    }

    private GenreRow mapGenre(ResultSet resultSet, int rowNumber) throws SQLException {
        return new GenreRow(resultSet.getLong("id"), resultSet.getString("name"));
    }

    private BookRow mapBook(ResultSet resultSet, int rowNumber) throws SQLException {
        return new BookRow(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getLong("author_id"),
                resultSet.getString("author_full_name"),
                resultSet.getLong("genre_id"),
                resultSet.getString("genre_name"));
    }

    private BookCommentRow mapBookComment(ResultSet resultSet, int rowNumber) throws SQLException {
        return new BookCommentRow(
                resultSet.getLong("id"),
                resultSet.getString("text"),
                resultSet.getLong("book_id"));
    }

    private static <T> ItemWriter<T> saveAllWriter(CrudRepository<T, ?> repository) {
        return chunk -> repository.saveAll(new ArrayList<>(chunk.getItems()));
    }
}
