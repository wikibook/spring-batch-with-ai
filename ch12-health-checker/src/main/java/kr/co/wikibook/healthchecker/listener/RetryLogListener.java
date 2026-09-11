package kr.co.wikibook.healthchecker.listener;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.Retryable;

public class RetryLogListener implements RetryListener {

  private final Logger logger = LoggerFactory.getLogger(RetryLogListener.class);

  @Override
  public void beforeRetry(RetryPolicy retryPolicy, Retryable<?> retryable) {
    logger.info("beforeRetry: {}", retryable.getName());
  }

  @Override
  public void onRetrySuccess(
      RetryPolicy retryPolicy, Retryable<?> retryable,
      @Nullable Object result) {
    logger.info("onRetrySuccess: {}, result={}", retryable.getName(), result);
  }

  @Override
  public void onRetryFailure(
      RetryPolicy retryPolicy, Retryable<?> retryable,
      Throwable throwable) {
    logger.info("onRetryFailure: {}, {}", retryable.getName(), throwable.toString());
  }

  @Override
  public void onRetryPolicyExhaustion(
      RetryPolicy retryPolicy, Retryable<?> retryable,
      RetryException exception) {
    logger.info("onRetryPolicyExhaustion: {}, {}", retryable.getName(), exception.toString());
  }
}
