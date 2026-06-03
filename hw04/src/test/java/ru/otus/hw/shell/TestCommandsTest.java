package ru.otus.hw.shell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.TestRunnerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.shell.interactive.enabled=false",
        "spring.shell.noninteractive.enabled=false",
        "spring.shell.script.enabled=false"
})
class TestCommandsTest {

    @MockitoBean
    private TestRunnerService testRunnerService;

    @MockitoBean
    private LocalizedIOService localizedIOService;

    @Autowired
    private TestCommands testCommands;

    @Test
    @DisplayName("Should start testing and return localized completion message")
    void shouldStartTestingAndReturnLocalizedMessage() {
        // Arrange
        when(localizedIOService.getMessage("ShellCommands.test.completed"))
                .thenReturn("Testing completed.");

        // Act
        var commandResult = testCommands.startTest();

        // Assert
        verify(testRunnerService).run();
        assertThat(commandResult).isEqualTo("Testing completed.");
    }
}
