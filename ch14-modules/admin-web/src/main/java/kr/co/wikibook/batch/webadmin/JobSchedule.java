package kr.co.wikibook.batch.webadmin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobSchedule {
  private final JobService service;

  public JobSchedule(JobService service) {
    this.service = service;
  }

  @Scheduled(cron = "0 0,10 0 * * 0", zone = "Asia/Seoul")
  public void startHelloJob() {
    start("helloJob");
  }

  @Scheduled(cron = "0 * * * * ?", zone = "Asia/Seoul")
  public void startSpendTimeChunkJob() {
    start("spendTimeChunkJob");
  }

  private void start(String jobName) {
    var jobParameters = new Properties();
    long scheduledTime = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
    jobParameters.put("scheduledTime", scheduledTime + ",java.lang.Long,true");
    service.start(jobName, jobParameters);
  }
}
