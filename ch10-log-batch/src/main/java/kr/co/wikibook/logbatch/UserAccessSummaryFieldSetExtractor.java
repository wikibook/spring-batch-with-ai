package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.file.transform.FieldExtractor;

public class UserAccessSummaryFieldSetExtractor implements FieldExtractor<UserAccessSummary> {
  public Object[] extract(UserAccessSummary summary) {
    return new Object[] {summary.username(), summary.accessCount()};
  }
}
