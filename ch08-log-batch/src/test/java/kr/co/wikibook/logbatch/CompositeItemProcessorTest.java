package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.support.CompositeItemProcessor;

class CompositeItemProcessorTest {

  @DisplayName("여러 프로세서를 순서대로 거쳐 아이템을 가공한다")
  @Test
  void compositeProcess() throws Exception {
    var compositeProcessor = new CompositeItemProcessor<Integer, Integer>();
    compositeProcessor.setDelegates(List.of(plus2Processor(), multiply10Processor()));
    compositeProcessor.afterPropertiesSet();

    Integer processed = compositeProcessor.process(1);
    assertThat(processed).isEqualTo(30); // <1>
  }

  ItemProcessor<Integer, Integer> plus2Processor() {
    return (item) -> item + 2;
  }

  ItemProcessor<Integer, Integer> multiply10Processor() {
    return (item) -> item * 10;
  }
}
