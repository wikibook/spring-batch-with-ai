package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemWriter;

class ClassifierCompositeItemWriterTest {

  @DisplayName("아이템을 세 라이터에 번갈아 나눠 쓴다")
  @Test
  void compositeWrite() throws Exception {
    // given
    var delegate1 = new ListItemWriter<Integer>();
    var delegate2 = new ListItemWriter<Integer>();
    var delegate3 = new ListItemWriter<Integer>();

    var compositeWriter = new ClassifierCompositeItemWriter<Integer>();
    compositeWriter.setClassifier(new RoundRobinClassifier<>(
        List.of(delegate1, delegate2, delegate3)
    ));

    // when
    compositeWriter.write(Chunk.of(1, 2, 3, 4, 5));

    // then
    assertThat(delegate1.getWrittenItems()).isEqualTo(List.of(1, 4));
    assertThat(delegate2.getWrittenItems()).isEqualTo(List.of(2, 5));
    assertThat(delegate3.getWrittenItems()).isEqualTo(List.of(3));
  }
}