package kr.co.wikibook.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

class ReportFormatDeciderTest {
  @ParameterizedTest
  @MethodSource("provideDateAndReportFormat")
  void decide(LocalDate reportDate, ReportFormat format) {
    var jobParameters = new JobParametersBuilder()
        .addLocalDate("reportDate", reportDate)
        .toJobParameters();
    JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("testJob", 0L, 0L, jobParameters);
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobExecution, "testStep", 0L);
    FlowExecutionStatus executionStatus = new ReportFormatDecider().decide(jobExecution, stepExecution);

    assertThat(executionStatus.getName()).isEqualTo(format.name());
  }

  static Stream<Arguments> provideDateAndReportFormat() {
    return Stream.of(
        Arguments.of(LocalDate.of(2026, 7, 15), ReportFormat.DAILY),
        Arguments.of(LocalDate.of(2026, 7, 20), ReportFormat.WEEKLY),
        Arguments.of(LocalDate.of(2026, 7, 1), ReportFormat.MONTHLY),
        Arguments.of(LocalDate.of(2026, 6, 1), ReportFormat.MONTHLY) // 1일이면서 월요일
    );
  }
}