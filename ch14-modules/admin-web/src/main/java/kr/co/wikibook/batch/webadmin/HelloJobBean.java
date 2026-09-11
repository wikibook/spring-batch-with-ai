package kr.co.wikibook.batch.webadmin;

import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class HelloJobBean extends QuartzJobBean {
  private final JobService service;

  public HelloJobBean(JobService service) {
    this.service = service;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) {
    var jobParameters = new Properties();
    long scheduledTime = context.getScheduledFireTime().getTime();
    jobParameters.put("scheduledTime", scheduledTime + ",java.lang.Long,true");
    service.start("helloJob", jobParameters);
  }
}
