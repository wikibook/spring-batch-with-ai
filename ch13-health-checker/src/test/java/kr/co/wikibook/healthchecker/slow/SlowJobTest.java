package kr.co.wikibook.healthchecker.slow;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.wikibook.healthchecker.HealthCheckerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class SlowJobTest {
  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier("slowJob") Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLong("limit", 1L)
        .toJobParameters();

    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.STOPPED);
  }
}
