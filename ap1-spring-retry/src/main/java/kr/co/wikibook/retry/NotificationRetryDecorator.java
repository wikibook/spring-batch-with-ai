package kr.co.wikibook.retry;

import java.time.Duration;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryOperations;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;

public class NotificationRetryDecorator implements NotificationService {
  private final NotificationService target;
  private final RetryOperations retryOperations;

  public NotificationRetryDecorator(NotificationService target, int maxRetries) {
    this.target = target;

    var retryPolicy = RetryPolicy.builder()
        .includes(RuntimeException.class)
        .maxRetries(maxRetries)
        .delay(Duration.ofMillis(100L))
        .multiplier(2.0d)
        .maxDelay(Duration.ofSeconds(5L))
        .build();

    var retryTemplate = new RetryTemplate(retryPolicy);
    retryTemplate.setRetryListener(new RetryLoggingListener());
    this.retryOperations = retryTemplate;
  }

  @Override
  public void send(String message) {
    try {
      this.retryOperations.execute((Retryable<Void>) () -> {
        target.send(message);
        return null;
      });
    } catch (RetryException ex) {
      throw new RuntimeException(ex);
    }
  }
}
