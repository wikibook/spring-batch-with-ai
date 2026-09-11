package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

class AccessLogCsvReaderTest {
  Logger logger = LoggerFactory.getLogger(this.getClass());

  @DisplayName("CSV 파일의 모든 줄을 한 건씩 읽는다")
  @Test
  void read() throws IOException {
    // given
    var resource = new ClassPathResource("2026-07-28.csv");
    var reader = new AccessLogCsvReader(resource);

    // when
    reader.open();
    int itemCount = 0;
    AccessLog item;
    while ((item = reader.read()) != null) {
      itemCount++;
      logger.info("{}", item);
    }
    reader.close();

    // then
    assertThat(itemCount).isEqualTo(3);
  }
}