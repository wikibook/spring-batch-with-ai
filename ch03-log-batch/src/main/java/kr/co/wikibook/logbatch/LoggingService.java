package kr.co.wikibook.logbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingService implements NotificationService {

  private final Logger logger = LoggerFactory.getLogger(LoggingService.class);

  @Override
  public void send(String message) {
    logger.info(message);
  }
}
