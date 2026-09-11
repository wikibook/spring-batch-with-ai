package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemWriter;

class CompositeItemWriterTest {
  @DisplayName("같은 아이템을 여러 라이터에 모두 쓴다")
  @Test
  void compositeWrite() throws Exception {
    // given
    var delegate1 = new ListItemWriter<Integer>();
    var delegate2 = new ListItemWriter<Integer>();
    var compositeWriter = new CompositeItemWriter<Integer>();
    compositeWriter.setDelegates(List.of(delegate1, delegate2));
    compositeWriter.afterPropertiesSet();
    List<Integer> items = List.of(1, 2, 3);

    // when
    compositeWriter.write(new Chunk<>(items));

    // then
    assertThat(delegate1.getWrittenItems()).isEqualTo(items);
    assertThat(delegate2.getWrittenItems()).isEqualTo(items);
  }
}
