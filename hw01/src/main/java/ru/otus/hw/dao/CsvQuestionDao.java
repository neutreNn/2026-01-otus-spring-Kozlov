package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try {
            var resourceStream = getClass().getClassLoader()
                    .getResourceAsStream(fileNameProvider.getTestFileName());
            if (resourceStream == null) {
                throw new QuestionReadException("Resource file not found: " + fileNameProvider.getTestFileName());
            }

            try (var reader = new InputStreamReader(resourceStream)) {
                var csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                        .withType(QuestionDto.class)
                        .withSeparator(';')
                        .withSkipLines(1)
                        .build();

                return csvToBean.stream()
                        .map(QuestionDto::toDomainObject)
                        .collect(Collectors.toList());
            }
        } catch (QuestionReadException e) {
            throw e;
        } catch (Exception e) {
            throw new QuestionReadException("Error reading questions from CSV file", e);
        }
    }
}
