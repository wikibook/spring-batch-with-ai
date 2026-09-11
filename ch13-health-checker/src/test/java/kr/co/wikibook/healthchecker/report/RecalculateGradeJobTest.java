package kr.co.wikibook.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
@SpringBatchTest
class RecalculateGradeJobTest {
  @Test
  void startJob(
      @Autowired JobOperatorTestUtils testUtils,
      @Autowired @Qualifier("recalculateGradeJob") Job job
  ) throws Exception {
    testUtils.setJob(job);
    JobExecution execution = testUtils.startJob();
    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(execution.getStepExecutions())
        .extracting(StepExecution::getStepName)
        .containsExactlyInAnyOrder(
            "recalculateGradeManagerStep",
            "recalculateGradeStep:partition0",
            "recalculateGradeStep:partition1",
            "recalculateGradeStep:partition2",
            "recalculateGradeStep:partition3"
        );
  }
}
