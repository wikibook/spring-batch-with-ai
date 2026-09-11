package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

@SpringBootTest({
    "date=2026-07-28",
    "base-path=build/test-output/"
})
class AccessLogJobTest {
  static Path basePath = Path.of("build/test-output");
  static Path output = basePath.resolve("2026-07-28_summary.csv");

  @BeforeAll
  static void prepareBasePath() throws IOException {
    Files.createDirectories(basePath);
    Files.copy(
        Path.of("src/test/resources/2026-07-28.csv"),
        basePath.resolve("2026-07-28.csv"),
        StandardCopyOption.REPLACE_EXISTING
    );
    Files.deleteIfExists(output);
  }

  @DisplayName("잡을 실행하면 DB 쓰기와 CSV 출력이 모두 끝난다")
  @Test
  void startJob(@Autowired DataSource dataSource) throws IOException {
    int count = JdbcTestUtils.countRowsInTable(new JdbcTemplate(dataSource), "access_log");
    assertThat(count).isGreaterThan(0);
    assertThat(Files.exists(output)).isTrue();
    assertThat(Files.readAllLines(output)).isNotEmpty();
  }
}
