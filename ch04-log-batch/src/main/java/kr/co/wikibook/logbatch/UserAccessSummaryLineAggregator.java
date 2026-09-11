package kr.co.wikibook.logbatch;

public class UserAccessSummaryLineAggregator {
  public String aggregate(UserAccessSummary summary) {
    return String.format("%s,%d", summary.username(), summary.accessCount());
  }
}