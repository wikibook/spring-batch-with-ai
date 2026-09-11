package kr.co.wikibook.logbatch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest("spring.batch.job.enabled=false")
@ActiveProfiles("test")
class LogBatchApplicationTests {
  @Test
  void contextLoads() {
  }
}
