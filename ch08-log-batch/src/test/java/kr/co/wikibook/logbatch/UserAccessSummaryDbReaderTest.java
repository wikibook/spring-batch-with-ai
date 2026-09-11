package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(TestDbConfig.class) // <1>
@Transactional
class UserAccessSummaryDbReaderTest {

  static final String INSERT = "INSERT INTO access_log (access_date_time, ip, username) VALUES "; // <2>
  UserAccessSummaryDbReader reader;

  @DisplayName("사용자별 접속 건수를 집계해서 한 건씩 읽는다")
  @Test
  @Sql(statements = {
      INSERT + "('2026-07-28 11:14', '175.242.91.54', 'benelog')",
      INSERT + "('2026-07-28 11:15', '192.168.0.1', 'benelog')",
      INSERT + "('2026-07-28 11:16', '192.168.0.3', 'jojoldu')"
  }) // <3>
  void readItems(@Autowired DataSource dataSource) throws SQLException { // <4>
    // given
    this.reader = new UserAccessSummaryDbReader(dataSource, LocalDate.of(2026, 7, 28));

    // when
    this.reader.open(new ExecutionContext());
    UserAccessSummary item1 = this.reader.read();
    UserAccessSummary item2 = this.reader.read();
    UserAccessSummary item3 = this.reader.read();

    // then
    assertThat(item1.username()).isEqualTo("benelog");
    assertThat(item1.accessCount()).isEqualTo(2);
    assertThat(item2.username()).isEqualTo("jojoldu");
    assertThat(item2.accessCount()).isEqualTo(1);
    assertThat(item3).isNull();
  }

  @AfterEach
  void close() {
    this.reader.close();
  }
}
