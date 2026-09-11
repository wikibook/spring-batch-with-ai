package kr.co.wikibook.batch.report.job;


import kr.co.wikibook.batch.report.ReportFormat;
import kr.co.wikibook.batch.report.ReportFormatDecider;
import kr.co.wikibook.batch.report.tasklet.LoggingTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        .on(ReportFormat.DAILY.name())
        .to(buildStep("일간 보고서 생성"))

        .from(formatDecider)
        .on(ReportFormat.WEEKLY.name())
        .to(buildStep("주간 보고서 생성"))

        .from(formatDecider)
        .on(ReportFormat.MONTHLY.name())
        .to(buildStep("월간 보고서 생성"))

        .from(formatDecider)
        .on("*")
        .fail()
        .end()
        .build();
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"))
        .build();
  }
}
