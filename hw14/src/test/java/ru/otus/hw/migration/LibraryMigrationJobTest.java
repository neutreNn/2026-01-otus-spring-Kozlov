package ru.otus.hw.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.models.mongo.BookCommentDocument;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.repositories.mongo.AuthorDocumentRepository;
import ru.otus.hw.repositories.mongo.BookCommentDocumentRepository;
import ru.otus.hw.repositories.mongo.BookDocumentRepository;
import ru.otus.hw.repositories.mongo.GenreDocumentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBatchTest
@SpringBootTest(properties = {
        "spring.batch.job.enabled=false",
        "spring.shell.interactive.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.shell.boot.StandardCommandsAutoConfiguration",
        "spring.data.mongodb.database=library-batch-test",
        "de.flapdoodle.mongodb.embedded.version=7.0.14"
})
class LibraryMigrationJobTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AuthorDocumentRepository authorRepository;

    @Autowired
    private GenreDocumentRepository genreRepository;

    @Autowired
    private BookDocumentRepository bookRepository;

    @Autowired
    private BookCommentDocumentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void shouldMigrateLibraryDataFromSqlToMongoKeepingRelations() throws Exception {
        var jobExecution = jobLauncherTestUtils.launchJob(newJobParameters());

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        var authors = authorRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        var genres = genreRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        var books = bookRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        var comments = commentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        assertThat(authors)
                .extracting(AuthorDocument::getId, AuthorDocument::getFullName)
                .containsExactly(
                        tuple(1L, "Author_1"),
                        tuple(2L, "Author_2"),
                        tuple(3L, "Author_3"));

        assertThat(genres)
                .extracting(GenreDocument::getId, GenreDocument::getName)
                .containsExactly(
                        tuple(1L, "Genre_1"),
                        tuple(2L, "Genre_2"),
                        tuple(3L, "Genre_3"));

        assertThat(books)
                .extracting(BookDocument::getId, BookDocument::getTitle)
                .containsExactly(
                        tuple(1L, "BookTitle_1"),
                        tuple(2L, "BookTitle_2"),
                        tuple(3L, "BookTitle_3"));

        assertThat(books)
                .extracting(book -> book.getAuthor().getId(), book -> book.getGenre().getId())
                .containsExactly(tuple(1L, 1L), tuple(2L, 2L), tuple(3L, 3L));

        assertThat(comments)
                .extracting(BookCommentDocument::getId, BookCommentDocument::getText, BookCommentDocument::getBookId)
                .containsExactly(
                        tuple(1L, "Comment_1", 1L),
                        tuple(2L, "Comment_2", 1L),
                        tuple(3L, "Comment_3", 2L));

        var repeatedJobExecution = jobLauncherTestUtils.launchJob(newJobParameters());

        assertThat(repeatedJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(authorRepository.count()).isEqualTo(3);
        assertThat(genreRepository.count()).isEqualTo(3);
        assertThat(bookRepository.count()).isEqualTo(3);
        assertThat(commentRepository.count()).isEqualTo(3);
    }

    private static JobParameters newJobParameters() {
        return new JobParametersBuilder()
                .addLong("run.id", System.nanoTime())
                .toJobParameters();
    }
}
