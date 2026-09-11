package kr.co.wikibook.batch.admincli;

import kr.co.wikibook.batch.hello.HelloJobGroupContexts;
import kr.co.wikibook.batch.report.ReportJobGroupContexts;
import kr.co.wikibook.batch.support.BatchConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({BatchConfig.class, HelloJobGroupContexts.class, ReportJobGroupContexts.class})
public class AdminCliApplication {
  static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(AdminCliApplication.class, args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }
}
