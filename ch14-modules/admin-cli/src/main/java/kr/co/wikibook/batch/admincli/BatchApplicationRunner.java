package kr.co.wikibook.batch.admincli;

import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "jobName")
public class BatchApplicationRunner implements ApplicationRunner {
  private final JobService service;
  private final String jobName;

  public BatchApplicationRunner(@Value("${jobName}") String jobName, JobService service) {
    this.jobName = jobName;
    this.service = service;
  }

  @Override
  public void run(ApplicationArguments args) {
    String[] jobArguments = args.getNonOptionArgs().toArray(new String[0]);
    Properties params = StringUtils.splitArrayElementsIntoProperties(jobArguments, "=");
    service.start(jobName, params);
  }
}
