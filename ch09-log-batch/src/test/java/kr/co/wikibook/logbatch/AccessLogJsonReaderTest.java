package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.json.JsonItemReader;
import org.springframework.core.io.ClassPathResource;

class AccessLogJsonReaderTest {
  @DisplayName("JSON 파일의 한 건을 AccessLog 객체로 읽는다")
  @Test
  void read() throws Exception {
    var resource = new ClassPathResource("sample-access-log.json");
    JsonItemReader<AccessLog> reader = JsonComponents.buildJsonItemReader(resource);
    reader.open(new ExecutionContext());
    AccessLog item = reader.read();
    reader.close();

    assertThat(item.accessDateTime()).isEqualTo("2026-07-28T11:14:16Z");
    assertThat(item.ip()).isEqualTo("175.242.91.54");
    assertThat(item.username()).isEqualTo("benelog");
  }
}
