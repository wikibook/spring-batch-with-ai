package kr.co.wikibook.batch.webadmin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DuplicateStartTest {

  @Autowired
  JobService service;

  @Test
  void rejectSameJobParameters() {
    var jobParameters = new Properties();
    jobParameters.put("scheduledTime", Instant.now().toEpochMilli() + ",java.lang.Long,true");

    service.start("helloJob", jobParameters);

    // 같은 잡 파라미터로 다시 시작하면 이미 있는 JobInstance와 겹쳐서 거부된다
    assertThatThrownBy(() -> service.start("helloJob", jobParameters))
        .isInstanceOf(RuntimeException.class);
  }
}
