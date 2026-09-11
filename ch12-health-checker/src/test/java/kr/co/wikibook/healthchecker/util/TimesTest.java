package kr.co.wikibook.healthchecker.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TimesTest {

  @ParameterizedTest
  @CsvSource(value = {
      "2026-07-28T13:00:00, 2026-07-28T13:01:00, 0:01:00",
      "2026-07-28T13:00:00, 2026-07-28T14:01:50, 1:01:50",
      "2026-07-28T13:40:40, 2026-07-28T14:10:21, 0:29:41" // <1>
  })
  void getReadableDuration(LocalDateTime from, LocalDateTime to, String expected) {
    String actual = Times.getReadableDuration(from, to);
    assertThat(actual).isEqualTo(expected);
  }
}
