package kr.co.wikibook.healthchecker.backup;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackupRouteTest {
  @TempDir
  Path tempPath;

  @Test
  void invalidSourceDirectory() {
    Path nonExistentPath = Path.of("noex");
    assertThatThrownBy(
        () -> new BackupRoute(
            nonExistentPath,
            this.tempPath
        )
    ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'sourceDirectory' must be a directory.");
  }

  @Test
  void invalidTargetParentDirectory() {
    Path nonExistentPath = Path.of("noex");
    assertThatThrownBy(
        () -> new BackupRoute(
            this.tempPath,
            nonExistentPath
        )
    ).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'targetParentDirectory' must be a directory.");
  }
}
