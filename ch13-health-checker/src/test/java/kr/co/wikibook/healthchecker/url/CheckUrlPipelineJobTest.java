package kr.co.wikibook.healthchecker.url;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class CheckUrlPipelineJobTest {

  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired Job checkUrlPipelineJob
  ) throws Exception {
    testUtils.setJob(checkUrlPipelineJob);
    var urls = new ClassPathResource("ok-urls.txt");
    JobParameters params = testUtils.getUniqueJobParametersBuilder()
        .addString(CheckUrlPipelineJobConfig.INPUT_FILE_PARAM, urls.getFile().getPath())
        .toJobParameters();

    JobExecution execution = testUtils.startJob(params);

    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
