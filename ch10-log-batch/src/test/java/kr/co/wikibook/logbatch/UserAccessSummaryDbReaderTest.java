package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(SharedConnectionDbConfig.class)
@Transactional
class UserAccessSummaryDbReaderTest {

  static final String INSERT = "INSERT INTO access_log (access_date_time, ip, username) VALUES ";

  @DisplayName("사용자별 접속 건수를 집계해서 한 건씩 읽는다")
  @Test
  @Sql(statements = {
      INSERT + "('2026-07-28 11:14', '175.242.91.54', 'benelog')",
      INSERT + "('2026-07-28 11:15', '192.168.0.1', 'benelog')",
      INSERT + "('2026-07-28 11:16', '192.168.0.3', 'jojoldu')"
  })
    // <3>
  void readItems(@Autowired DataSource dataSource) throws Exception {
    // given
    var date = LocalDate.of(2026, 7, 28);
    JdbcCursorItemReader<UserAccessSummary> reader = UserAccessSummaryComponents.buildDbCursorReader(
        dataSource, date, true
    );

    // when
    reader.open(new ExecutionContext());
    UserAccessSummary item1 = reader.read();
    UserAccessSummary item2 = reader.read();
    UserAccessSummary item3 = reader.read();

    // then
    assertThat(item1.username()).isEqualTo("benelog");
    assertThat(item1.accessCount()).isEqualTo(2);
    assertThat(item2.username()).isEqualTo("jojoldu");
    assertThat(item2.accessCount()).isEqualTo(1);
    assertThat(item3).isNull();
  }
}
