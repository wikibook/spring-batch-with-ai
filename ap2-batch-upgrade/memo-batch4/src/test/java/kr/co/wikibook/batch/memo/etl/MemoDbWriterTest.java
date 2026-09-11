package kr.co.wikibook.batch.memo.etl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class MemoDbWriterTest {

  @Test
  void writeMemo(@Autowired DataSource mainDataSource) throws Exception {
    // given
    JdbcBatchItemWriter<String> writer = MemoComponents.memoDbWriter(mainDataSource);
    int beforeCount = countRowsInTable(mainDataSource);

    // when
    writer.write(List.of("hello", "world", "batch"));

    // then
    int afterCount = countRowsInTable(mainDataSource);
    assertThat(beforeCount + 3).isEqualTo(afterCount);
  }

  private static int countRowsInTable(DataSource dataSource) {
    return JdbcTestUtils.countRowsInTable(new JdbcTemplate(dataSource), "memo");
  }
}
