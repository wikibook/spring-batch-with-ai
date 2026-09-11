package kr.co.wikibook.logbatch;

import java.nio.file.Path;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;

@Configuration
@ConditionalOnProperty("date")
public class AccessLogJobConfig {

  private final LocalDate date;
  private final DataSource dataSource;
  private final Path basePath;

  public AccessLogJobConfig(
      @Value("${date}") LocalDate date,
      DataSource dataSource,
      @Value("${base-path:./logs}") Path basePath) {
    this.date = date;
    this.dataSource = dataSource;
    this.basePath = basePath;
  }

  @Bean
  @Order(1)
  public CommandLineRunner accessLogCsvToDbTask() {
    var resource = new FileSystemResource(basePath.resolve(date + ".csv"));
    var reader = new AccessLogCsvReader(resource);
    var writer = new AccessLogDbWriter(dataSource);
    return new AccessLogCsvToDbTask(reader, writer, 300);
  }

  @Bean
  @Order(2)
  public CommandLineRunner userAccessSummaryDbToCsvTask() {
    var reader = new UserAccessSummaryDbReader(dataSource, date);
    var resource = new FileSystemResource(basePath.resolve(date + "_summary.csv"));
    var writer = new UserAccessSummaryCsvWriter(resource);
    return new UserAccessSummaryDbToCsvTask(reader, writer, 300);
  }
}
