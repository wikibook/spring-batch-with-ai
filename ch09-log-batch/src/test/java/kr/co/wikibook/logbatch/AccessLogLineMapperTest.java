package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLogLineMapperTest {

  @DisplayName("CSV 한 줄을 AccessLog 객체로 변환한다")
  @Test
  void mapLine() throws Exception {
    // given
    var line = "2026-07-28 12:14:16,175.242.91.54,benelog";
    LineMapper<AccessLog> lineMapper = buildAccessLogLineMapper();

    // when
    AccessLog log = lineMapper.mapLine(line, 1);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2026-07-28T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }

  LineMapper<AccessLog> buildAccessLogLineMapper() {
    var lineMapper = new DefaultLineMapper<AccessLog>();
    lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
    lineMapper.setFieldSetMapper(new AccessLogFieldSetMapper());
    return lineMapper;
  }
}
