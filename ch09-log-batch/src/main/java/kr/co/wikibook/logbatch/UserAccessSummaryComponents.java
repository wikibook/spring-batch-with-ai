package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.FieldExtractor;
import org.springframework.batch.infrastructure.item.file.transform.RecordFieldExtractor;
import org.springframework.core.io.WritableResource;

public class UserAccessSummaryComponents {
  public static FlatFileItemWriter<UserAccessSummary> buildCsvWriter(WritableResource resource) {
    FlatFileItemWriter<UserAccessSummary> writer =  new FlatFileItemWriterBuilder<UserAccessSummary>()
        .name("userAccessSummaryCsvWriter")
        .resource(resource)
        .delimited()
        .delimiter(",")
        .sourceType(UserAccessSummary.class)
        .names("username", "accessCount")
        .build();
    return Configs.afterPropertiesSet(writer);
  }

  // UserAccessSummaryFieldSetExtractor 클래스의 역할을 대신할 수 있음
  static FieldExtractor<UserAccessSummary> buildUserAccessSummaryFieldSetExtractor() {
    var fieldExtractor = new RecordFieldExtractor<>(UserAccessSummary.class);
    fieldExtractor.setNames("username", "accessCount");
    return fieldExtractor;
  }
}
