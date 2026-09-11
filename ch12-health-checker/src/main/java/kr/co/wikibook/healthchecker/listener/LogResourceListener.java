package kr.co.wikibook.healthchecker.listener;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class LogResourceListener {

  private final Logger logger = LoggerFactory.getLogger(LogResourceListener.class);
  private final Resource resource;

  public LogResourceListener(FileSystemResource resource) {
    this.resource = resource;
  }

  @BeforeJob
  public void logLastModified() throws IOException {
    File file = this.resource.getFile();
    Instant lastModified = Instant.ofEpochMilli(file.lastModified());
    this.logger.info("{} 파일 마지막 수정: {}", file.getName(), lastModified);
  }
}
