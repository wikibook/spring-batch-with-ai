package kr.co.wikibook.logbatch;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;

public class AccessLogFieldSetMapper implements FieldSetMapper<AccessLog> {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  @Override
  public AccessLog mapFieldSet(FieldSet fieldSet) {
    String firstField = fieldSet.readString(0);
    Instant accessDateTime = Instant.from(FORMATTER.parse(firstField));
    String ip = fieldSet.readString(1);
    String username = fieldSet.readString(2);
    return new AccessLog(accessDateTime, ip, username);
  }
}
