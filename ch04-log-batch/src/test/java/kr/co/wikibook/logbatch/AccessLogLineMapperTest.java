package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccessLogLineMapperTest {
  @DisplayName("CSV 한 줄을 AccessLog 객체로 변환한다")
  @Test
  void mapLine() {
    // given
    var line = "2026-07-28 12:14:16,175.242.91.54,benelog";
    var lineMapper = new AccessLogLineMapper();

    // when
    AccessLog log = lineMapper.mapLine(line);

    // then
    assertThat(log.accessDateTime()).isEqualTo("2026-07-28T12:14:16Z"); // <1>
    assertThat(log.ip()).isEqualTo("175.242.91.54");
    assertThat(log.username()).isEqualTo("benelog");
  }
}