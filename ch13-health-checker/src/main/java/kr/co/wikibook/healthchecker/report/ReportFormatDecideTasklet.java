package kr.co.wikibook.healthchecker.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class ReportFormatDecideTasklet implements Tasklet, StepExecutionListener { // <1>

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
    LocalDate reportDate = jobParameters.getLocalDate("reportDate");

    ReportFormat reportFormat = getReportFormat(reportDate);
    ExecutionContext executionContext = contribution.getStepExecution().getExecutionContext();
    executionContext.put("reportFormat", reportFormat); // <3>

    return RepeatStatus.FINISHED;
  }

  private ReportFormat getReportFormat(LocalDate reportDate) { // <4>
    if (reportDate.getDayOfMonth() == 1) {
      return ReportFormat.MONTHLY;
    }

    if (reportDate.getDayOfWeek() == DayOfWeek.MONDAY) {
      return ReportFormat.WEEKLY;
    }

    return ReportFormat.DAILY;
  }

  public ExitStatus afterStep(StepExecution stepExecution) { // <5>
    ExecutionContext executionContext = stepExecution.getExecutionContext();
    ReportFormat reportType = (ReportFormat) executionContext.get("reportFormat"); // <6>
    return new ExitStatus(reportType.name()); // <7>
  }
}