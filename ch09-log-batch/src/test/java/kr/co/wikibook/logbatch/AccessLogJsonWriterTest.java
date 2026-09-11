package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.json.JsonFileItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;

class AccessLogJsonWriterTest {
  @DisplayName("AccessLog를 JSON 한 줄로 파일에 쓴다")
  @Test
  void write(@TempDir Path tempPath) throws Exception {
    // given
    Path outputPath = tempPath.resolve("access-log.json");
    WritableResource resource = new FileSystemResource(outputPath);
    JsonFileItemWriter<AccessLog> writer = JsonComponents.buildJsonItemWriter(resource);
    var item = new AccessLog(Instant.parse("2026-07-28T11:14:16Z"), "127.0.0.1", "benelog");

    // when
    writer.open(new ExecutionContext());
    writer.write(Chunk.of(item));
    writer.close();

    // then
    String jsonOutput = Files.readString(outputPath);
    assertThat(jsonOutput).contains("""
        {"accessDateTime":"2026-07-28T11:14:16Z","ip":"127.0.0.1","username":"benelog"}
        """);
  }
}
