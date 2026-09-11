package kr.co.wikibook.batch.report.job;

import kr.co.wikibook.batch.report.tasklet.LoggingTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class UserRankingJobConfig {
  private final JobRepository jobRepository;

  public UserRankingJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job userRankingJob() {
    return new JobBuilder("userRankingJob", jobRepository)
        .start(processAccessLogFlow())
        .split(new SimpleAsyncTaskExecutor())
        .add(analyzePurchasesFlow())
        .next(buildStep("사용자 순위 기록"))
        .end()
        .build();
  }

  @Bean
  public Flow processAccessLogFlow() {
    return new FlowBuilder<SimpleFlow>("접근 기록 처리")
        .start(buildStep("액세스 로그 전처리"))
        .next(buildStep("액세스 로그 분석"))
        .build();
  }

  @Bean
  public Flow analyzePurchasesFlow() {
    return new FlowBuilder<SimpleFlow>("구매 내역 분석")
        .start(buildStep("구매액 합계 계산"))
        .build();
  }

  private Step buildStep(String stepName) {
    var tasklet = new LoggingTasklet(stepName + " 수행");
    return new StepBuilder(stepName, jobRepository)
        .tasklet(tasklet)
        .build();
  }
}
