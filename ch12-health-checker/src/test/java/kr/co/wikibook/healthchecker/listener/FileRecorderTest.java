package kr.co.wikibook.healthchecker.listener;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.ExecutionContext;

class FileRecorderTest {

  @Test
  void write(@TempDir Path baseDir) throws IOException {
    Path recordPath = baseDir.resolve("record.txt");
    var recorder = new FileRecorder(recordPath);

    recorder.open(new ExecutionContext());
    recorder.writeLine("Hello");
    recorder.writeLine("batch");
    recorder.close();

    String content = Files.readString(recordPath);
    assertThat(content).isEqualTo("Hello\nbatch\n");
  }
}
