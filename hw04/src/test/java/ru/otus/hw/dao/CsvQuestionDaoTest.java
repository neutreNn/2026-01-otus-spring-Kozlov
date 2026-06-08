package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CsvQuestionDao.class)
class CsvQuestionDaoTest {

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private QuestionDao questionDao;

    @Test
    @DisplayName("Should return questions list from test-questions.csv file")
    void shouldReturnQuestionsFromTestResource() {
        // Arrange
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");

        // Act
        var questions = questionDao.findAll();

        // Assert
        assertThat(questions).isNotNull();
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).text()).isEqualTo("What is 2+2?");
        assertThat(questions.get(0).answers()).hasSize(4);
        assertThat(questions.get(1).text()).isEqualTo("What is the capital of Russia?");
        assertThat(questions.get(1).answers()).hasSize(4);
    }
}
