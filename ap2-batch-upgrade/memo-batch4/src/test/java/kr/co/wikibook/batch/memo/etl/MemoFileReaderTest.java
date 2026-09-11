package kr.co.wikibook.batch.memo.etl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.PathResource;

class MemoFileReaderTest {

  @Test
  void readMemo(@TempDir Path tempPath) throws Exception {
    // given
    Path memoFile = tempPath.resolve(Path.of("memo.txt"));
    Files.write(memoFile, List.of("hello", "batch", "Hi, batch"));

    // when, then
    FlatFileItemReader<String> reader = MemoComponents.memoFileReader(new PathResource(memoFile));
    reader.open(new ExecutionContext());
    assertThat(reader.read()).isEqualTo("hello");
    assertThat(reader.read()).isEqualTo("batch");
    assertThat(reader.read()).isEqualTo("Hi, batch");
    assertThat(reader.read()).isNull();
    reader.close();
  }
}
