package kr.co.wikibook.healthchecker.backup;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.util.FileSystemUtils;

public class BackupDailyTask implements Callable<RepeatStatus> {

  private final Logger logger = LoggerFactory.getLogger(BackupDailyTask.class);
  private final BackupRoute route;
  private final Clock clock;

  public BackupDailyTask(BackupRoute route, Clock clock) {
    this.route = route;
    this.clock = clock;
  }

  @Override
  public RepeatStatus call() throws IOException {
    Path sourceDirectory = route.getSourceDirectory().toRealPath();
    Path targetParentDirectory = route.getTargetParentDirectory().toRealPath();
    if (targetParentDirectory.startsWith(sourceDirectory)) {
      throw new IllegalArgumentException("'targetParentDirectory' must be outside 'sourceDirectory'.");
    }
    LocalDate today = LocalDate.now(clock);
    String targetDirectoryName = sourceDirectory.getFileName() + "_" + today;
    Path targetDirectory = targetParentDirectory.resolve(targetDirectoryName);
    targetDirectory.toFile().mkdir();

    FileSystemUtils.copyRecursively(sourceDirectory, targetDirectory);
    logger.info("Backup completed from {} to {}", sourceDirectory, targetDirectory);
    return RepeatStatus.FINISHED;
  }
}
