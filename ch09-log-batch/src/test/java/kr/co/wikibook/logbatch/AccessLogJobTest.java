package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class AccessLogJobTest {
  @DisplayName("잡을 실행하면 첫 스텝이 세 건을 쓴다")
  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier(AccessLogJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("date", LocalDate.of(2026, 7, 28))
        .toJobParameters();

    JobExecution execution = testUtils.startJob(params);

    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
    Collection<StepExecution> stepExecutions = execution.getStepExecutions();
    StepExecution firstStep = stepExecutions.iterator().next();
    assertThat(firstStep.getWriteCount()).isEqualTo(3);
  }
}
