package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    @DisplayName("Should execute test and print all questions with answers")
    void shouldExecuteTestAndPrintQuestionsWithAnswers() {
        // Arrange
        var answer1 = new Answer("2", false);
        var answer2 = new Answer("3", false);
        var answer3 = new Answer("4", true);
        var answer4 = new Answer("5", false);

        var question1 = new Question("What is 2+2?",
            List.of(answer1, answer2, answer3, answer4));

        var answer5 = new Answer("London", false);
        var answer6 = new Answer("Berlin", false);
        var answer7 = new Answer("Moscow", true);
        var answer8 = new Answer("Madrid", false);

        var question2 = new Question("What is the capital of Russia?",
            List.of(answer5, answer6, answer7, answer8));

        var questions = List.of(question1, question2);
        when(questionDao.findAll()).thenReturn(questions);

        // Act
        testService.executeTest();

        // Assert
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService).printFormattedLine("%d. %s", 1, "What is 2+2?");
        verify(ioService).printFormattedLine("   %d) %s", 1, "2");
        verify(ioService).printFormattedLine("   %d) %s", 2, "3");
        verify(ioService).printFormattedLine("   %d) %s", 3, "4");
        verify(ioService).printFormattedLine("   %d) %s", 4, "5");
        verify(ioService).printFormattedLine("%d. %s", 2, "What is the capital of Russia?");
        verify(ioService).printFormattedLine("   %d) %s", 1, "London");
        verify(ioService).printFormattedLine("   %d) %s", 2, "Berlin");
        verify(ioService).printFormattedLine("   %d) %s", 3, "Moscow");
        verify(ioService).printFormattedLine("   %d) %s", 4, "Madrid");
    }

    @Test
    @DisplayName("Should call QuestionDao.findAll() exactly once")
    void shouldCallQuestionDaoFindAllOnce() {
        // Arrange
        var questions = List.of(
            new Question("Question?", List.of(new Answer("Answer", true)))
        );
        when(questionDao.findAll()).thenReturn(questions);

        // Act
        testService.executeTest();

        // Assert
        verify(questionDao).findAll();
    }

    @Test
    @DisplayName("Should print header message before questions")
    void shouldPrintHeaderMessage() {
        // Arrange
        var questions = List.of(
            new Question("Q1?", List.of(new Answer("A1", true)))
        );
        when(questionDao.findAll()).thenReturn(questions);

        // Act
        testService.executeTest();

        // Assert
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(questionDao).findAll();
    }
}

