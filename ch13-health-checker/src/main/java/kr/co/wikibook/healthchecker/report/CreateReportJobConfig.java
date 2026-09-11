package kr.co.wikibook.healthchecker.report;


import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CreateReportJobConfig {
  private final JobRepository jobRepository;

  public CreateReportJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job createReportJob() {
    var formatDecider = new ReportFormatDecider();
    return new JobBuilder("createReportJob", jobRepository)
        .start(formatDecider)

        .on(ReportFormat.DAILY.name()) // <2>
        .to(buildStep("일간 보고서 생성"))

        .from(formatDecider) // <2>
        .on(ReportFormat.WEEKLY.name())
        .to(buildStep("주간 보고서 생성"))

        .from(formatDecider) // <3>
        .on(ReportFormat.MONTHLY.name())
        .to(buildStep("월간 보고서 생성"))

        .from(formatDecider)
        .on("*")
        .fail() // <4>
        .end()
        .build();
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"))
        .build();
  }
}
