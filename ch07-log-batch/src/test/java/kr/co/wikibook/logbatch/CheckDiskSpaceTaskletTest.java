package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

class CheckDiskSpaceTaskletTest {

  @DisplayName("남은 디스크 공간이 기준에 못 미치면 예외를 던진다")
  @Test
  void checkDiskSpace() {
    // given
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("directory", "/")
        .addLong("minUsablePercentage", 100L)
        .toJobParameters();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    var tasklet = new CheckDiskSpaceTasklet();

    // when, then
    assertThatThrownBy(() ->
        tasklet.execute(stepContribution, chunkContext)
    ).isInstanceOf(IllegalStateException.class);

    ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
    long usablePercentage = jobExecutionContext.getLong("usablePercentage"); // <1>
    assertThat(usablePercentage).isGreaterThan(0L);
  }
}
