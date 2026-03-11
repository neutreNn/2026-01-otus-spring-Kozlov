package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvQuestionDaoTest {

    @Test
    @DisplayName("Should throw QuestionReadException when resource not found")
    void shouldThrowExceptionWhenResourceNotFound() {
        // Arrange
        var appProperties = new AppProperties("non-existent-file.csv");
        var dao = new CsvQuestionDao(appProperties);

        // Act & Assert
        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class);
    }

    @Test
    @DisplayName("Should return questions list from test-questions.csv file")
    void shouldReturnQuestionsFromTestResource() {
        // Arrange
        var appProperties = new AppProperties("test-questions.csv");
        var dao = new CsvQuestionDao(appProperties);

        // Act
        var questions = dao.findAll();

        // Assert
        assertThat(questions).isNotNull();
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).text()).isEqualTo("What is 2+2?");
        assertThat(questions.get(0).answers()).hasSize(4);
        assertThat(questions.get(1).text()).isEqualTo("What is the capital of Russia?");
        assertThat(questions.get(1).answers()).hasSize(4);
    }
}

