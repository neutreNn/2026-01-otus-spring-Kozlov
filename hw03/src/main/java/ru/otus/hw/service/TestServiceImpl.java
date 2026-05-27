package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        printTestHeader();
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (int i = 0; i < questions.size(); i++) {
            askQuestion(i + 1, questions.get(i), testResult);
        }
        return testResult;
    }

    private void printTestHeader() {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");
    }

    private void askQuestion(int questionNumber, Question question, TestResult testResult) {
        ioService.printFormattedLine("%d. %s", questionNumber, question.text());
        for (int i = 0; i < question.answers().size(); i++) {
            ioService.printFormattedLine("   %d) %s", i + 1, question.answers().get(i).text());
        }

        var answerNumber = readAnswerNumber(question.answers().size());
        var isAnswerValid = question.answers().get(answerNumber - 1).isCorrect();
        testResult.applyAnswer(question, isAnswerValid);
        ioService.printLine("");
    }

    private int readAnswerNumber(int answersCount) {
        return ioService.readIntForRangeWithPrompt(
                1,
                answersCount,
                ioService.getMessage("TestService.input.answer.number"),
                ioService.getMessage("TestService.error.answer.number", answersCount)
        );
    }
}
