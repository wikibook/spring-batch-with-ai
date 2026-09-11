package kr.co.wikibook.retry;

import org.springframework.resilience.annotation.Retryable;

interface RetryableNotificationService extends NotificationService {
  @Retryable(
      includes = RuntimeException.class,
      maxRetries = 4,
      delay = 100L, multiplier = 2d, maxDelay = 5000L
  )
  void send(String message);

  int getTryCount();
}
