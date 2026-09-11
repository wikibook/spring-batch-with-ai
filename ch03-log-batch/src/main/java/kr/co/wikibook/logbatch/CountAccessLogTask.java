package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CountAccessLogTask implements CommandLineRunner {

  private final JdbcOperations jdbc;
  private final NotificationService notificationService;

  public CountAccessLogTask(DataSource dataSource, NotificationService notificationService) { // <2>
    this.jdbc = new JdbcTemplate(dataSource); // <3>
    this.notificationService = notificationService;
  }

  @Override
  public void run(String... args) {
    Long count = jdbc.queryForObject("SELECT COUNT(1) FROM access_log", Long.class);
    notificationService.send("access_log 테이블의 건수 : " + count);
  }
}
