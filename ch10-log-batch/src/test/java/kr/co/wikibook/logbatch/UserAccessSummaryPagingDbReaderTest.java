package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = TestDbConfig.class)
@Transactional
class UserAccessSummaryPagingDbReaderTest {

  @DisplayName("페이징 리더로 집계 결과를 페이지 단위로 읽는다")
  @Test
  @Sql("classpath:/access-log.sql")
  void readItems(@Autowired DataSource dataSource) throws Exception {
    // given
    var date = LocalDate.of(2026, 7, 28);
    JdbcPagingItemReader<UserAccessSummary> reader = UserAccessSummaryComponents.buildDbPagingReader(
        dataSource, date, 2
    );

    // when
    UserAccessSummary item1 = reader.read();
    UserAccessSummary item2 = reader.read();
    UserAccessSummary item3 = reader.read();
    reader.close();

    // then
    assertThat(item1.username()).isEqualTo("benelog");
    assertThat(item1.accessCount()).isEqualTo(2);
    assertThat(item2.username()).isEqualTo("jojoldu");
    assertThat(item2.accessCount()).isEqualTo(1);
    assertThat(item3).isNull();
  }
}
