package kr.co.wikibook.batch.webadmin;

import kr.co.wikibook.batch.hello.HelloJobGroupContexts;
import kr.co.wikibook.batch.report.ReportJobGroupContexts;
import kr.co.wikibook.batch.support.BatchConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import({BatchConfig.class, HelloJobGroupContexts.class, ReportJobGroupContexts.class})
public class AdminWebApplication {
  static void main(String[] args) {
    SpringApplication.run(AdminWebApplication.class, args);
  }
}
