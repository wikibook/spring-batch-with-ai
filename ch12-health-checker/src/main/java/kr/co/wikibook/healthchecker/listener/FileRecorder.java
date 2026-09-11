package kr.co.wikibook.healthchecker.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStream;
import org.springframework.batch.infrastructure.item.ItemStreamException;

public class FileRecorder implements ItemStream {
  private final Path recordPath;
  private FileWriter writer;

  public FileRecorder(Path recordPath) {
    this.recordPath = recordPath;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    try {
      this.writer = new FileWriter(recordPath.toFile(), StandardCharsets.UTF_8, true);
    } catch (IOException ex) {
      throw new ItemStreamException(ex);
    }
  }

  protected void writeLine(String line) {
    try {
      writer.write(line);
      writer.write('\n');
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public void close() {
    try {
      writer.close();
    } catch (IOException ex) {
      throw new ItemStreamException(ex);
    }
  }
}
