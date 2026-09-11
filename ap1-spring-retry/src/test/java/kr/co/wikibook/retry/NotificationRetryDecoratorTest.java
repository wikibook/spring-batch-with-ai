package kr.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationRetryDecoratorTest {

  @Test
  void successByRetry() {
    // given
    var target = new UnstableNotificationService(3);
    var decorator = new NotificationRetryDecorator(target, 4);

    // when
    decorator.send("hello");

    // then
    assertThat(target.getTryCount()).isEqualTo(4);
  }
}
