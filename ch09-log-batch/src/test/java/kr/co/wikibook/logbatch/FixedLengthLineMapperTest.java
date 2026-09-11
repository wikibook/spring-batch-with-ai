package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.file.LineMapper;

class FixedLengthLineMapperTest {
  @DisplayName("고정 길이 한 줄을 AccessLog 객체로 변환한다")
  @Test
  void mapLine() throws Exception {
    var line = "2026-07-28 12:14:16 175.242.91.54   benelog   ";
    LineMapper<AccessLog> lineMapper = LineMapperComponents.buildAccessLogLineMapper();

    AccessLog log = lineMapper.mapLine(line, 0);

    assertThat(log.accessDateTime()).isEqualTo("2026-07-28T12:14:16Z");
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}
