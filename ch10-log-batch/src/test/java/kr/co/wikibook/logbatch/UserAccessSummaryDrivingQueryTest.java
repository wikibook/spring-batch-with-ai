package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestDbConfig.class)
class UserAccessSummaryDrivingQueryTest {

  static final String INSERT = "INSERT INTO access_log (access_date_time, ip, username) VALUES ";

  @DisplayName("드라이빙 쿼리로 사용자명을 읽고 프로세서에서 접속 건수를 조회한다")
  @Test
  @Sql(statements = {
      INSERT + "('2026-07-28 11:14', '175.242.91.54', 'benelog')",
      INSERT + "('2026-07-28 11:15', '192.168.0.1', 'benelog')",
      INSERT + "('2026-07-28 11:16', '192.168.0.3', 'jojoldu')"
  })
  @Sql(statements = "DELETE FROM access_log",
      executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // <1>
  void readAndProcess(@Autowired DataSource dataSource) throws Exception {
    // given
    var date = LocalDate.of(2026, 7, 28);
    JdbcCursorItemReader<String> reader =
        UserAccessSummaryComponents.buildUsernameReader(dataSource, date);
    ItemProcessor<String, UserAccessSummary> processor =
        UserAccessSummaryComponents.buildAccessCountProcessor(dataSource, date);

    // when
    reader.open(new ExecutionContext());
    UserAccessSummary item1 = processor.process(reader.read());
    UserAccessSummary item2 = processor.process(reader.read());
    String key3 = reader.read();
    reader.close();

    // then
    assertThat(item1).isEqualTo(new UserAccessSummary("benelog", 2));
    assertThat(item2).isEqualTo(new UserAccessSummary("jojoldu", 1));
    assertThat(key3).isNull();
  }
}
