package kr.co.wikibook.healthchecker.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

class CheckDiskSpaceTaskletTest {
  @Test
  void checkDiskSpace(@TempDir Path baseDir) throws IOException {
    // given
    Path source = baseDir.resolve("source");
    source.toFile().mkdir();
    Path txtFile = source.resolve("test.txt");
    Files.writeString(txtFile, "T".repeat(12)); // UTF-8로 12 bytes 파일

    Path targetParentDir = baseDir.resolve("target");
    targetParentDir.toFile().mkdir();
    long actualUsableSpace = Files.getFileStore(targetParentDir).getUsableSpace();
    var tasklet = new CheckDiskSpaceTasklet(new BackupRoute(source, targetParentDir));

    JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(
        jobExecution, "testStep", 1L
    );
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));

    // when
    RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

    // then
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long usableSpace = executionContext.getLong("usableSpace");
    assertThat(usableSpace).isEqualTo(actualUsableSpace);
    long sourceSize = executionContext.getLong("sourceSize");
    assertThat(sourceSize).isEqualTo(12L);
    long executionCount = executionContext.getInt("executionCount");
    assertThat(executionCount).isEqualTo(1);

    tasklet.execute(stepContribution, chunkContext); // 2번째 실행
    executionCount = executionContext.getInt("executionCount");
    assertThat(executionCount).isEqualTo(2);
  }
}
