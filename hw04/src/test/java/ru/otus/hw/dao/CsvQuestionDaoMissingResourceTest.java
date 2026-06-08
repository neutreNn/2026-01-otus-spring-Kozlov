package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CsvQuestionDao.class)
class CsvQuestionDaoMissingResourceTest {

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private QuestionDao questionDao;

    @Test
    @DisplayName("Should throw QuestionReadException when resource not found")
    void shouldThrowExceptionWhenResourceNotFound() {
        // Arrange
        when(fileNameProvider.getTestFileName()).thenReturn("non-existent-file.csv");

        // Act & Assert
        assertThatThrownBy(questionDao::findAll)
                .isInstanceOf(QuestionReadException.class);
    }
}
