package kr.co.wikibook.healthchecker.listener;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.springframework.core.retry.RetryListener;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.Retryable;

public class RetryItemRecorder extends FileRecorder implements RetryListener {
  public RetryItemRecorder(Path recordPath) {
    super(recordPath);
  }

  @Override
  public void onRetrySuccess(
      RetryPolicy retryPolicy, Retryable<?> retryable,
      @Nullable Object result
  ) {
    if (result == null) {
      return;
    }
    writeLine(result.toString());
  }
}
