package kr.co.wikibook.batch.report.job;

import java.time.LocalDate;
import kr.co.wikibook.batch.report.tasklet.HolidayCheckTask;
import kr.co.wikibook.batch.report.tasklet.LoggingTasklet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendReportJobConfig {
  private final JobRepository jobRepository;

  public SendReportJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job sendReportJob() {
    return new JobBuilder("sendReportJob", jobRepository)
        .start(checkHolidayStep())
        .on("FAILED") // <1>
        .to(buildStep("보고서 전송 없이 종료"))

        .from(checkHolidayStep()) // <2>
        .on("*") // <3>
        .to(buildStep("보고서 전송"))
        .end()
        .build();
  }

  @Bean
  public Step checkHolidayStep() {
    return new StepBuilder("checkHolidayStep", jobRepository)
        .tasklet(checkHolidayTasklet(null))
        .build();
  }

  @Bean
  @StepScope
  public Tasklet checkHolidayTasklet(
      @Value("#{jobParameters['reportDate']}") LocalDate reportDate) {
    var task = new HolidayCheckTask(reportDate);
    return new CallableTaskletAdapter(task);
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"))
        .build();
  }
}
