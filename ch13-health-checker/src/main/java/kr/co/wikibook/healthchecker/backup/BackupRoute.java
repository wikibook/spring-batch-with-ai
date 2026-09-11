package kr.co.wikibook.healthchecker.backup;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

@JobScope
public class BackupRoute {
  private final Path sourceDirectory;
  private final Path targetParentDirectory;

  public BackupRoute(
      @Value("#{jobParameters['sourceDirectory']}") Path sourceDirectory,
      @Value("#{jobParameters['targetParentDirectory']}") Path targetParentDirectory) {
    Assert.isTrue(Files.isDirectory(sourceDirectory),
        "'sourceDirectory' must be a directory."
    );
    Assert.isTrue(Files.isDirectory(targetParentDirectory),
        "'targetParentDirectory' must be a directory."
    );
    this.sourceDirectory = sourceDirectory;
    this.targetParentDirectory = targetParentDirectory;
  }

  public Path getSourceDirectory() {
    return sourceDirectory;
  }

  public Path getTargetParentDirectory() {
    return targetParentDirectory;
  }
}
