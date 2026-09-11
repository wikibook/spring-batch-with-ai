package kr.co.wikibook.batch.webadmin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HelloJobEndToEndTest {

  @Test
  void startJobSynchronously(
      @Autowired Job helloJob,
      @Autowired JobRepository jobRepository
  ) throws Exception {
    var testUtils = JobTestSupports.getJobOperatorTestUtils(helloJob, jobRepository);

    JobExecution execution = testUtils.startJob();

    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
