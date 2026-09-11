package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.infrastructure.item.support.PassThroughItemProcessor;

class ClassifierCompositeItemProcessorTest {
  @DisplayName("홀수와 짝수에 서로 다른 프로세서를 적용한다")
  @Test
  void oddEvenProcessor() throws Exception {
    var compositeProcessor = new ClassifierCompositeItemProcessor<Integer, Integer>();
    compositeProcessor.setClassifier(new OddEvenClassifier<>(
        (Integer item) -> item * 2,
        new PassThroughItemProcessor<>()
    ));
    assertThat(compositeProcessor.process(3)).isEqualTo(6);
    assertThat(compositeProcessor.process(4)).isEqualTo(4);
  }
}
