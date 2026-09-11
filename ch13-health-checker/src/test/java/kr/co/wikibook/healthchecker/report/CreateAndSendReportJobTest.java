package kr.co.wikibook.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
class CreateAndSendReportJobTest {

  JobOperatorTestUtils testUtils;

  @BeforeEach
  void setUp(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier("createAndSendReportJob") Job job) {
    testUtils.setJob(job);
    this.testUtils = testUtils;
  }

  @Test
  void startJobOnWorkday() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2025, 5, 2))
        .addLong("runId", Instant.now().toEpochMilli())
        .toJobParameters();

    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }

  @Test
  void startJobOnHoliday() throws Exception {
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addLocalDate("reportDate", LocalDate.of(2025, 5, 5))
        .addLong("runId", Instant.now().toEpochMilli())
        .toJobParameters();

    JobExecution execution = testUtils.startJob(params);
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }
}
