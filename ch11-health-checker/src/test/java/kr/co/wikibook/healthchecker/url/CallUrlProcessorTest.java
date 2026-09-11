package kr.co.wikibook.healthchecker.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CallUrlProcessorTest {
  @Test
  @DisplayName("200 OK 응답을 받는다")
  void processOk() throws Exception {
    CallUrlProcessor processor = new CallUrlProcessor(Duration.ofSeconds(3));
    ResponseStatus responseStatus = processor.process("https://benelog.net");
    assertThat(responseStatus.statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("404 Not Found 응답을 받는다")
  void processNotFound() throws Exception {
    CallUrlProcessor processor = new CallUrlProcessor(Duration.ofSeconds(3));
    ResponseStatus responseStatus = processor.process("https://benelog.net/t");
    assertThat(responseStatus.statusCode()).isEqualTo(404);
  }

  @Test
  @DisplayName("타임아웃 예외가 발생한다")
  void processWhenTimeout() {
    CallUrlProcessor processor = new CallUrlProcessor(Duration.ofMillis(1));
    assertThatExceptionOfType(HttpConnectTimeoutException.class)
        .isThrownBy(() ->
            processor.process("https://benelog.net")
        );
  }

  @Test
  @DisplayName("부적절한 URI 형식이라 예외가 발생한다")
  void processWhenInvalidUriFormat() {
    CallUrlProcessor processor = new CallUrlProcessor(Duration.ofMillis(2000));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() ->
            processor.process("benelog.net")
        );
  }
}
