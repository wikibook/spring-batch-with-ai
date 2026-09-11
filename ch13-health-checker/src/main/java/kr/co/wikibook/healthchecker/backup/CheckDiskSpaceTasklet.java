package kr.co.wikibook.healthchecker.backup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class CheckDiskSpaceTasklet implements Tasklet {
  private final BackupRoute route;

  public CheckDiskSpaceTasklet(BackupRoute route) {
    this.route = route;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws IOException {
    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
    ExecutionContext executionContext = jobExecution.getExecutionContext();

    long sourceSize = getDirectorySize(route.getSourceDirectory());
    executionContext.putLong("sourceSize", sourceSize);
    long usableSpace = Files.getFileStore(route.getTargetParentDirectory()).getUsableSpace();
    executionContext.putLong("usableSpace", usableSpace);
    int executionCount = executionContext.getInt("executionCount", 0);
    executionCount++;
    executionContext.putInt("executionCount", executionCount);

    return RepeatStatus.FINISHED;
  }

  private long getDirectorySize(Path directory) throws IOException {
    try (Stream<Path> files = Files.walk(directory, FileVisitOption.FOLLOW_LINKS)) {
      return files
          .mapToLong(this::sizeIfRegularFile)
          .sum();
    }
  }

  private long sizeIfRegularFile(Path path) {
    try {
      BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
      return attributes.isRegularFile() ? attributes.size() : 0;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
