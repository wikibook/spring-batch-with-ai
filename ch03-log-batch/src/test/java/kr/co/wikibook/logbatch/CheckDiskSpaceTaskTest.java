package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckDiskSpaceTaskTest {

  MockNotificationService notificationService = new MockNotificationService();
  CheckDiskSpaceTask task = new CheckDiskSpaceTask(notificationService, 1);

  @DisplayName("인자가 없으면 아무것도 하지 않는다")
  @Test
  void doNothingWhenEmptyArgument() throws IOException {
    task.run();
  }

  @DisplayName("디스크 용량이 기대치보다 많다")
  @Test
  void checkDiskSpaceWhenSufficient() throws IOException {
    task.run("/");
    String message = notificationService.getLastMessage();
    assertThat(message).matches("남은 용량 \\d{1,3}%");
  }

  @DisplayName("디스크 용량이 기대치보다 적다")
  @Test
  void checkDiskSpaceWhenInsufficient() {
    CheckDiskSpaceTask insufficientTask = new CheckDiskSpaceTask(notificationService, 100);
    assertThatThrownBy(() ->
        insufficientTask.run("/")
    ).isInstanceOf(IllegalStateException.class);
  }
}
