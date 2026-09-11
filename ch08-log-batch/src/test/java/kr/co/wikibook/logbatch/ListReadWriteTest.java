package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.batch.infrastructure.item.support.ListItemWriter;

class ListReadWriteTest {

  @DisplayName("리스트의 아이템을 하나씩 읽고 다 읽으면 null을 반환한다")
  @Test
  void read() {
    var reader = new ListItemReader<>(List.of(1, 2));
    assertThat(reader.read()).isEqualTo(1);
    assertThat(reader.read()).isEqualTo(2);
    assertThat(reader.read()).isNull();
  }


  @DisplayName("쓴 아이템이 리스트에 그대로 쌓인다")
  @Test
  void write() throws Exception {
    var writer = new ListItemWriter<Integer>();
    List<Integer> items = List.of(1, 2, 3);
    writer.write(new Chunk<>(items));
    assertThat(writer.getWrittenItems()).isEqualTo(items);
  }
}
