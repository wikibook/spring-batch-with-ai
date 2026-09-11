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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class HelloChunkJobTest {
  @DisplayName("helloChunkJob을 실행하면 COMPLETED 상태로 끝난다")
  @Test
  void launchJob(
          @Autowired JobOperatorTestUtils testUtils,
          @Autowired @Qualifier(HelloChunkJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .toJobParameters();
    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }

  @DisplayName("같은 잡을 새 파라미터로 다시 실행해도 정상 종료된다")
  @Test
  void launchJob2(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier(HelloChunkJobConfig.JOB_NAME) Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .toJobParameters();
    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
