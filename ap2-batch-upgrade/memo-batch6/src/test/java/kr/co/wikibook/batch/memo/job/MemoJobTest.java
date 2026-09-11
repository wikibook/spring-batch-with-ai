package kr.co.wikibook.batch.memo.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBatchTest
class MemoJobTest {

  @Autowired
  JobOperatorTestUtils jobOperatorTestUtils;

  @Autowired
  Job memoJob;

  @BeforeEach
  void setUp() {
    this.jobOperatorTestUtils.setJob(memoJob);
  }

  @Test
  void launchJob(@TempDir Path tempPath) throws Exception {
    Path memoFile = tempPath.resolve(Path.of("memo.txt"));
    Files.write(memoFile, List.of("hello", "world", "batch"));
    JobParameters parameters = new JobParametersBuilder()
        .addString("memoFile", "file:" + memoFile.toAbsolutePath())
        .toJobParameters();

    JobExecution execution = jobOperatorTestUtils.startJob(parameters);

    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    StepExecution stepExecution = execution.getStepExecutions().iterator().next();
    assertThat(stepExecution.getWriteCount()).isEqualTo(3);
  }
}
