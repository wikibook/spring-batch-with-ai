package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@SpringBatchTest
//@Transactional
class HelloJobTest {
  @DisplayName("잡을 실행하면 runId 파라미터가 자동으로 채워진다")
  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired Job helloJob
  ) throws Exception {
    testUtils.setJob(helloJob);
    JobExecution execution = testUtils.startJob();

    JobParameters parameters = execution.getJobParameters();
    assertThat(parameters.getLong("runId")).isNotNull(); // <3>
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
