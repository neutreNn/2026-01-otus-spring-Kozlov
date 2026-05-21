package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (int i = 0; i < questions.size(); i++) {
            var question = questions.get(i);
            ioService.printFormattedLine("%d. %s", i + 1, question.text());
            for (int j = 0; j < question.answers().size(); j++) {
                ioService.printFormattedLine("   %d) %s", j + 1, question.answers().get(j).text());
            }
            var answersCount = question.answers().size();
            var answerNumber = ioService.readIntForRangeWithPrompt(
                    1,
                    answersCount,
                    "Input answer number:",
                    "Please input number from 1 to " + answersCount
            );
            var isAnswerValid = question.answers().get(answerNumber - 1).isCorrect();
            testResult.applyAnswer(question, isAnswerValid);
            ioService.printLine("");
        }
        return testResult;
    }
}
