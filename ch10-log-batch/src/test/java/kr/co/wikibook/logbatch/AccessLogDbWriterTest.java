package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(TestDbConfig.class)
@Transactional
class AccessLogDbWriterTest {

  @DisplayName("AccessLog를 access_log 테이블에 쓴다")
  @Test
  void write(@Autowired DataSource dataSource) throws Exception {
    // given
    JdbcBatchItemWriter<AccessLog> writer = AccessLogComponents.buildAccessLogDbWriter(dataSource);
    var item = new AccessLog(Instant.now(), "127.0.0.1", "benelog");

    // when
    writer.write(new Chunk<>(List.of(item)));
    // then
    int count = JdbcTestUtils.countRowsInTableWhere(
        new JdbcTemplate(dataSource),
        "access_log",
        "ip='127.0.0.1' AND username='benelog'"
    );
    assertThat(count).isEqualTo(1);
  }
}
