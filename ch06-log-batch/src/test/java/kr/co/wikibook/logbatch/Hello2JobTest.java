package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest({
    "spring.batch.job.enabled=false",
    "spring.batch.job.name=" + Hello2JobConfig.JOB_NAME
})
@SpringBatchTest
class Hello2JobTest {
  @DisplayName("hello2Job을 실행하면 COMPLETED 상태로 끝난다")
  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired Job hello2Job
  ) throws Exception {
    testUtils.setJob(hello2Job);
    JobExecution execution = testUtils.startJob();
    assertThat(execution.getStatus()).isSameAs(BatchStatus.COMPLETED);
  }
}
