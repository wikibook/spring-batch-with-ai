package kr.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnstableNotificationService implements RetryableNotificationService {

  private final Logger logger = LoggerFactory.getLogger(UnstableNotificationService.class);
  private final int failures;
  private int tryCount = 0;

  public UnstableNotificationService(int failures) {
    this.failures = failures;
  }

  @Override
  public void send(String message) {
    this.tryCount++;
    if (this.tryCount <= this.failures) {
      throw new RuntimeException("실패 : " + tryCount);
    }
    logger.info("성공 : {}, {}", this.tryCount, message);
  }

  @Override
  public int getTryCount() {
    return this.tryCount;
  }
}
