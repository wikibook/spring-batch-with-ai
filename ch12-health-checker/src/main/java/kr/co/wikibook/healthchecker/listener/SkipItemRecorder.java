package kr.co.wikibook.healthchecker.listener;

import java.nio.file.Path;
import org.springframework.batch.core.listener.SkipListener;

public class SkipItemRecorder<T, S> extends FileRecorder implements SkipListener<T, S> {

  public SkipItemRecorder(Path recordPath) {
    super(recordPath);
  }

  public void onSkipInProcess(T item, Throwable throwable) {
    writeLine(item.toString());
  }
}
