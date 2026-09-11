package kr.co.wikibook.batch.webadmin;

import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzJobSchedule {
  @Bean
  public JobDetail helloJobDetail() {
    return JobBuilder.newJob(HelloJobBean.class)
        .withIdentity("helloJobDetail")
        .storeDurably(true)
        .build();
  }

  @Bean
  public Trigger helloJobCronTrigger() {
    var schedule = CronScheduleBuilder
        .cronSchedule("0 * * * * ?")
        .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

    return TriggerBuilder.newTrigger()
        .forJob(helloJobDetail())
        .withIdentity("helloJobCronTrigger")
        .startNow()
        .withSchedule(schedule)
        .build();
  }
}
