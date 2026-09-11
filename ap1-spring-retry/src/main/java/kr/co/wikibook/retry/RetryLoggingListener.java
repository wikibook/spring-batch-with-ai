package kr.co.wikibook.retry;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.Retryable;

public class RetryLoggingListener implements RetryListener {
	private final Logger logger = LoggerFactory.getLogger(RetryLoggingListener.class);

  @Override
  public void beforeRetry(@NonNull RetryPolicy retryPolicy, Retryable<?> retryable) {
    logger.info("beforeRetry {}", retryable.getName());
  }

  @Override
  public void onRetrySuccess(@NonNull RetryPolicy retryPolicy, Retryable<?> retryable, @Nullable Object result) {
    logger.info("onRetrySuccess {}", retryable.getName());
  }

  @Override
  public void onRetryFailure(@NonNull RetryPolicy retryPolicy, Retryable<?> retryable, Throwable throwable) {
    logger.info("onRetryFailure {}", retryable.getName());
  }
}
