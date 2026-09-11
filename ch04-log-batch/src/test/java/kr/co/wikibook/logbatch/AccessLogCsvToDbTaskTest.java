package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(TestDbConfig.class)
@Transactional
class AccessLogCsvToDbTaskTest {

  @DisplayName("CSV 파일을 읽어 access_log 테이블에 쓴다")
  @Test
  void runTask(@Autowired DataSource dataSource) throws Exception {
    // given
    CommandLineRunner task = new AccessLogJobConfig(
        LocalDate.of(2026, 7, 28),
        dataSource,
        Path.of("src/test/resources/")
    ).accessLogCsvToDbTask();

    // when
    task.run();

    // then
    int count = JdbcTestUtils.countRowsInTable(new JdbcTemplate(dataSource), "access_log");
    assertThat(count).isGreaterThanOrEqualTo(3);
  }
}
