package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(TestDbConfig.class) // <1>
@Transactional // <2>
class AccessLogDbWriterTest {

  @DisplayName("AccessLog를 access_log 테이블에 쓴다")
  @Test
  void write(@Autowired DataSource dataSource) { // <3>
    // given
    var writer = new AccessLogDbWriter(dataSource);
    var item = new AccessLog(Instant.now(), "127.0.0.1", "benelog");

    // when
    writer.write(List.of(item)); // <4>

    // then
    int count = JdbcTestUtils.countRowsInTableWhere(
        new JdbcTemplate(dataSource),
        "access_log",
        "ip='127.0.0.1' AND username='benelog'"
    ); // <5>
    assertThat(count).isEqualTo(1);
  }
}
