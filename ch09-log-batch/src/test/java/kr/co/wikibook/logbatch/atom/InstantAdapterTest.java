package kr.co.wikibook.logbatch.atom;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstantAdapterTest {
  InstantAdapter adapter = new InstantAdapter();
  String iso8601Text = "2025-08-13T02:07:36Z";
  Instant instant = Instant.parse(iso8601Text);

  @DisplayName("Instant를 ISO 8601 문자열로 변환한다")
  @Test
  void marshal() {
    String marshalled = adapter.marshal(instant);
    assertThat(marshalled).isEqualTo(iso8601Text);
  }

  @DisplayName("ISO 8601 문자열을 Instant로 변환한다")
  @Test
  void unmarshal() {
    Instant unmarshalled = adapter.unmarshal(iso8601Text);
    assertThat(unmarshalled).isEqualTo(instant);
  }
}
