package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAccessSummaryLineAggregatorTest {
  @DisplayName("집계 결과를 쉼표로 구분한 한 줄로 만든다")
  @Test
  void aggregate() {
    var lineAggregator = new UserAccessSummaryLineAggregator();
    UserAccessSummary summary = new UserAccessSummary("benelog", 3);

    String line = lineAggregator.aggregate(summary);

    assertThat(line).isEqualTo("benelog,3");
  }
}