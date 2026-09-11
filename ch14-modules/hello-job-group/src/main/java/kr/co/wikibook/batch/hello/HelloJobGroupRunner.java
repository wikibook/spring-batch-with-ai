package kr.co.wikibook.batch.hello;

import java.util.Properties;
import kr.co.wikibook.batch.support.BatchConfig;
import kr.co.wikibook.batch.support.JobService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.util.StringUtils;

@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@Import({BatchConfig.class, HelloJobGroupContexts.class})
@PropertySource("classpath:/job-db.properties")
public class HelloJobGroupRunner {
  static void main(String[] args) {
    String jobName = System.getProperty("jobName");
    Properties params = StringUtils.splitArrayElementsIntoProperties(args, "=");
    ApplicationContext context = new AnnotationConfigApplicationContext(HelloJobGroupRunner.class);
    JobService service = context.getBean(JobService.class);
    service.start(jobName, params);
  }
}
