package ru.otus.hw.commands;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class MigrationCommands {
    private final Job libraryMigrationJob;

    private final JobLauncher jobLauncher;

    private final JobOperator jobOperator;

    private final JobExplorer jobExplorer;

    public MigrationCommands(Job libraryMigrationJob,
                             JobLauncher jobLauncher,
                             JobOperator jobOperator,
                             JobExplorer jobExplorer) {
        this.libraryMigrationJob = libraryMigrationJob;
        this.jobLauncher = jobLauncher;
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
    }

    @ShellMethod(key = "start-migration", value = "Start SQL to MongoDB library migration")
    public String startMigration() throws Exception {
        var parameters = new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobLauncher.run(libraryMigrationJob, parameters);

        return formatExecution("Migration started", execution);
    }

    @ShellMethod(key = "restart-migration", value = "Restart failed migration by execution id")
    public String restartMigration(@ShellOption(help = "Failed job execution id") long executionId) throws Exception {
        var newExecutionId = jobOperator.restart(executionId);
        var execution = jobExplorer.getJobExecution(newExecutionId);

        if (execution == null) {
            return "Migration restarted: executionId=%d".formatted(newExecutionId);
        }

        return formatExecution("Migration restarted", execution);
    }

    @ShellMethod(key = "migration-status", value = "Show migration status by execution id")
    public String migrationStatus(@ShellOption(help = "Job execution id") long executionId) {
        var execution = jobExplorer.getJobExecution(executionId);

        if (execution == null) {
            return "Migration execution %d not found".formatted(executionId);
        }

        return formatExecution("Migration status", execution);
    }

    private static String formatExecution(String title, JobExecution execution) {
        return "%s: executionId=%d, jobInstanceId=%d, status=%s, exitStatus=%s"
                .formatted(
                        title,
                        execution.getId(),
                        execution.getJobInstance().getInstanceId(),
                        statusName(execution.getStatus()),
                        execution.getExitStatus().getExitCode());
    }

    private static String statusName(BatchStatus status) {
        return status == null ? "UNKNOWN" : status.name();
    }
}
