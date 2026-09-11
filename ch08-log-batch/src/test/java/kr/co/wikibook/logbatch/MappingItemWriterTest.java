package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemWriter;
import org.springframework.batch.infrastructure.item.support.MappingItemWriter;

class MappingItemWriterTest {
  record User(String name, int age) {}

  @DisplayName("아이템을 변환한 뒤 다음 라이터에 넘긴다")
  @Test
  void mapAndWrite() throws Exception {
    var downstream = new ListItemWriter<String>();
    var writer = new MappingItemWriter<Integer, String>(String::valueOf, downstream);

    writer.write(Chunk.of(1, 2, 3));

    assertThat(downstream.getWrittenItems()).isEqualTo(List.of("1", "2", "3"));
  }

  @DisplayName("레코드의 필드별로 다른 라이터에 나눠 쓴다")
  @Test
  void deconstructAndWrite() throws Exception {
    var nameWriter = new ListItemWriter<String>();
    var ageWriter = new ListItemWriter<Integer>();
    var compositeWriter = new CompositeItemWriter<User>();
    compositeWriter.setDelegates(List.of(
      new MappingItemWriter<User, String>(User::name, nameWriter),
      new MappingItemWriter<User, Integer>(User::age, ageWriter)
    ));
    compositeWriter.afterPropertiesSet();

    compositeWriter.write(Chunk.of(new User("kim", 20), new User("lee", 30)));

    assertThat(nameWriter.getWrittenItems()).isEqualTo(List.of("kim", "lee"));
    assertThat(ageWriter.getWrittenItems()).isEqualTo(List.of(20, 30));
  }
}
