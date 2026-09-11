package kr.co.wikibook.healthchecker.backup;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

public class DeleteOldDirectoriesTask implements Callable<RepeatStatus> {

  private final Logger logger = LoggerFactory.getLogger(DeleteOldDirectoriesTask.class);
  private final Path parentDirectory;
  private final int daysOfKeeping;
  private final Clock clock;

  public DeleteOldDirectoriesTask(Path parentDirectory, int daysOfKeeping, Clock clock) {
    this.parentDirectory = parentDirectory;
    this.daysOfKeeping = daysOfKeeping;
    this.clock = clock;
  }

  @Override
  public RepeatStatus call() throws IOException {
    Instant now = this.clock.instant();
    Instant baseInstant = now.minus(daysOfKeeping, ChronoUnit.DAYS);
    long baseEpochMilli = baseInstant.toEpochMilli();

    try (DirectoryStream<Path> paths = Files.newDirectoryStream(parentDirectory)) {
      for (Path path : paths) {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        if (!attributes.isDirectory()) {
          continue;
        }
        if (attributes.lastModifiedTime().toMillis() < baseEpochMilli) {
          boolean deleted = FileSystemUtils.deleteRecursively(path);
          if (!deleted) {
            throw new IOException("Failed to delete: " + path);
          }
          logger.info("Deleted : {}", path);
        }
      }
    }
    return RepeatStatus.FINISHED;
  }
}
