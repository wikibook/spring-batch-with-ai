package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.file.transform.LineAggregator;

public class UserAccessSummaryLineAggregator implements LineAggregator<UserAccessSummary> {
  @Override
  public String aggregate(UserAccessSummary summary) {
    return String.format("%s,%d", summary.username(), summary.accessCount());
  }
}
