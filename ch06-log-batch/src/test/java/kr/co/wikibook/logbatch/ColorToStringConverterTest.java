package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ColorToStringConverterTest {
  @DisplayName("Color 객체를 16진수 문자열로 변환한다")
  @Test
  void convertColorToString() {
    var converter = new ColorToStringConverter();
    Color color = Color.BLUE;
    String hex = converter.convert(color);
    assertThat(hex).isEqualTo("#0000FF");
  }
}