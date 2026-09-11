package kr.co.wikibook.healthchecker.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DatesTest {
  @ParameterizedTest
  @MethodSource("provideHolidays")
  void isHoliday(LocalDate day) {
    boolean actual = Dates.isHoliday(day);
    assertThat(actual).isTrue();
  }

  @ParameterizedTest
  @MethodSource("provideWorkingDays")
  void isWorkingDay(LocalDate day) {
    boolean actual = Dates.isHoliday(day);
    assertThat(actual).isFalse();
  }

  static Stream<LocalDate> provideHolidays() {
    return Stream.of(
        LocalDate.of(2026, 7, 18), // 토요일
        LocalDate.of(2026, 7, 19), // 일요일
        LocalDate.of(2026, 12, 25), // 크리스마스
        LocalDate.of(2026, 5, 5) // 어린이날
    );
  }

  static Stream<LocalDate> provideWorkingDays() {
    return Stream.of(
        LocalDate.of(2026, 7, 6),
        LocalDate.of(2026, 7, 13),
        LocalDate.of(2026, 7, 20)
    );
  }
}
