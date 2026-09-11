package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.ItemProcessor;

public class AccessLogProcessor implements ItemProcessor<AccessLog, AccessLog> {
  @Override
  public AccessLog process(AccessLog item) {
    if("127.0.0.1".equals(item.ip())) {
      return null;
    }
    return item;
  }
}