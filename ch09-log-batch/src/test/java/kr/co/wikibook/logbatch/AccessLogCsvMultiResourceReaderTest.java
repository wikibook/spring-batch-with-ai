package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;

class AccessLogCsvMultiResourceReaderTest {
  Logger logger = LoggerFactory.getLogger(this.getClass());

  @DisplayName("여러 CSV 파일을 이어서 한 건씩 읽는다")
  @Test
  void readItemsInMultiFile() throws Exception {
    // given
    MultiResourceItemReader<AccessLog> reader = AccessLogComponents.buildMultiResourceItemReader("classpath:/multi/*.csv");

    // when
    reader.open(new ExecutionContext());
    int itemCount = 0;
    AccessLog item;
    while ((item = reader.read()) != null) {
      itemCount++;
      logger.info("{}", item);
    }
    reader.close();

    // then
    assertThat(itemCount).isEqualTo(6);
  }
}
