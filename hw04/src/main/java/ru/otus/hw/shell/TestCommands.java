package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@RequiredArgsConstructor
public class TestCommands {

    private final TestRunnerService testRunnerService;

    private final LocalizedIOService localizedIOService;

    @ShellMethod(value = "Start student testing.", key = {"start-test", "st"})
    public String startTest() {
        testRunnerService.run();
        return localizedIOService.getMessage("ShellCommands.test.completed");
    }
}
