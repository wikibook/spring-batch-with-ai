package kr.co.wikibook.diskcheck;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiskSpaceMeterTest {

  DiskSpaceMeter meter = new DiskSpaceMeter();

  @DisplayName("남은 용량을 0에서 100 사이의 퍼센트로 계산한다")
  @Test
  void getUsablePercentage() throws IOException {
    int usablePercentage = meter.getUsablePercentage("/");
    assertThat(usablePercentage).isBetween(0, 100);
  }
}
