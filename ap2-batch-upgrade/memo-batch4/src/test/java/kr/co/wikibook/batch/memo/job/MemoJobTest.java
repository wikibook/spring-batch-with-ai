package kr.co.wikibook.batch.memo.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemoJobTest {

  JobLauncherTestUtils testUtils = new JobLauncherTestUtils();

  @BeforeEach
  void setUp(@Autowired JobRepository jobRepository,
             @Autowired JobLauncher jobLauncher,
             @Autowired Job memoJob) {
    this.testUtils.setJobRepository(jobRepository);
    this.testUtils.setJobLauncher(jobLauncher);
    this.testUtils.setJob(memoJob);
  }

  @Test
  void launchJob(@TempDir Path tempPath) throws Exception {
    Path memoFile = tempPath.resolve(Path.of("memo.txt"));
    Files.write(memoFile, List.of("hello", "world", "batch"));
    JobParameters parameters = new JobParametersBuilder()
        .addString("memoFile", "file:" + memoFile.toAbsolutePath())
        .toJobParameters();

    JobExecution execution = testUtils.launchJob(parameters);

    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    StepExecution stepExecution = execution.getStepExecutions().iterator().next();
    assertThat(stepExecution.getWriteCount()).isEqualTo(3);
  }
}
