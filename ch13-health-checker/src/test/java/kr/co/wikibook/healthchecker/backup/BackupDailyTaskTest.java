package kr.co.wikibook.healthchecker.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

class BackupDailyTaskTest {
  @Test
  void backupDailyDirectory(@TempDir Path baseDir) throws IOException {
    // given
    Path sourcePath = baseDir.resolve("source");
    sourcePath.toFile().mkdir();
    Files.writeString(sourcePath.resolve("test.txt"), "test content");
    Path targetParentPath = baseDir.resolve("backup");
    targetParentPath.toFile().mkdir();

    var task = new BackupDailyTask(
        new BackupRoute(sourcePath, targetParentPath),
        Clock.fixed(Instant.parse("2026-07-28T01:14:16Z"), ZoneOffset.UTC)
    );

    // when
    RepeatStatus status = task.call();

    // then
    assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    Path backupDirectory = targetParentPath.resolve("source_2026-07-28");
    Path testTxt = backupDirectory.resolve("test.txt");
    assertThat(Files.exists(testTxt)).isTrue();
  }
}
