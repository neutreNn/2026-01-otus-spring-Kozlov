package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.shell.interactive.enabled=false",
        "spring.shell.noninteractive.enabled=false",
        "spring.shell.script.enabled=false",
        "test.file-name-by-locale-tag[en-US]=non-existent-file.csv"
})
class CsvQuestionDaoMissingResourceTest {

    @Autowired
    private QuestionDao questionDao;

    @Test
    @DisplayName("Should throw QuestionReadException when resource not found")
    void shouldThrowExceptionWhenResourceNotFound() {
        // Act & Assert
        assertThatThrownBy(questionDao::findAll)
                .isInstanceOf(QuestionReadException.class);
    }
}
