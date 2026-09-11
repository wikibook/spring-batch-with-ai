package kr.co.wikibook.healthchecker.report;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 13장 '리스너를 활용한 분기' 절의 구성이다. ReportFormatDecideTasklet이 StepExecutionListener로 설정한
 * ExitStatus로 분기한다. JobExecutionDecider로 대체한 최종 구성인 CreateReportJobConfig와
 * 잡 이름이 겹치지 않도록 'createReportByListenerJob'으로 등록한다.
 */
@Configuration
public class CreateReportByListenerJobConfig {
  private final JobRepository jobRepository;

  public CreateReportByListenerJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job createReportByListenerJob() {
    return new JobBuilder("createReportByListenerJob", jobRepository)
        .start(reportFormatDecideStep())

        .on(ReportFormat.DAILY.name()) // <1>
        .to(buildStep("일간 보고서 생성(리스너 분기)"))

        .from(reportFormatDecideStep()) // <2>
        .on(ReportFormat.WEEKLY.name())
        .to(buildStep("주간 보고서 생성(리스너 분기)"))

        .from(reportFormatDecideStep()) // <3>
        .on(ReportFormat.MONTHLY.name())
        .to(buildStep("월간 보고서 생성(리스너 분기)"))

        .from(reportFormatDecideStep())
        .on("*")
        .fail() // <4>
        .end()
        .build();
  }

  @Bean // <5>
  public Step reportFormatDecideStep() {
    return new StepBuilder("reportFormatDecideStep", jobRepository)
        .tasklet(new ReportFormatDecideTasklet())
        .build();
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"))
        .build();
  }
}
