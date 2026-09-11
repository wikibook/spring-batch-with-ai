package kr.co.wikibook.logbatch;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.batch.infrastructure.item.file.LineMapper;

public class AccessLogLineMapper implements LineMapper<AccessLog> {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  @Override
  public AccessLog mapLine(String line, int lineNumber) {
    String[] attrs = line.split(",");
    Instant accessDateTime = Instant.from(FORMATTER.parse(attrs[0]));
    String ip = attrs[1];
    String username = attrs[2];
    return new AccessLog(accessDateTime, ip, username);
  }
}
