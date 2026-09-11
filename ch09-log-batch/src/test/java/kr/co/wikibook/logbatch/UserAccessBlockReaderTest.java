package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ByteArrayResource;

class UserAccessBlockReaderTest {
  @DisplayName("여러 줄로 된 레코드 하나를 UserAccessSummary 한 건으로 읽는다")
  @Test
  void read() throws Exception {
    var content = """
        USR;benelog
        ACC;2026-07-28 12:14
        ACC;2026-07-28 17:20
        USR;jojoldu
        ACC;2026-07-29 09:00
        """;
    var reader = new UserAccessBlockReader(
        new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));

    reader.open(new ExecutionContext());
    UserAccessSummary item1 = reader.read();
    UserAccessSummary item2 = reader.read();
    UserAccessSummary item3 = reader.read();
    reader.close();

    assertThat(item1).isEqualTo(new UserAccessSummary("benelog", 2));
    assertThat(item2).isEqualTo(new UserAccessSummary("jojoldu", 1));
    assertThat(item3).isNull();
  }

  @DisplayName("레코드가 USR 줄로 시작하지 않으면 예외를 던진다")
  @Test
  void readNotStartingWithUserLine() {
    var content = """
        ACC;2026-07-28 12:14
        USR;benelog
        """;
    var reader = new UserAccessBlockReader(
        new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
    reader.open(new ExecutionContext());

    assertThatThrownBy(reader::read)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("레코드는 USR 줄로 시작해야 한다.");

    reader.close();
  }
}
