package kr.co.wikibook.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

class ReportFormatDecideTaskletTest {
  @ParameterizedTest
  @MethodSource("provideDateAndReportFormat")
  void execute(LocalDate reportDate, ReportFormat format) {
    // given
    var jobParameters = new JobParametersBuilder()
        .addLocalDate("reportDate", reportDate)
        .toJobParameters();
    var stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    var stepContribution = new StepContribution(stepExecution);
    ReportFormatDecideTasklet tasklet = new ReportFormatDecideTasklet();

    // when
    tasklet.execute(stepContribution, chunkContext);
    ExitStatus exitStatus = tasklet.afterStep(stepExecution);

    // then
    assertThat(exitStatus.getExitCode()).isEqualTo(format.name());
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