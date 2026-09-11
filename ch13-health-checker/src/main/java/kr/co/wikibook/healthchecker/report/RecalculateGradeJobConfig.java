package kr.co.wikibook.healthchecker.report;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class RecalculateGradeJobConfig {
  private final JobRepository jobRepository;

  public RecalculateGradeJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job recalculateGradeJob() {
    return new JobBuilder("recalculateGradeJob", jobRepository)
        .start(recalculateGradeManagerStep())
        .build();
  }

  @Bean
  public Step recalculateGradeManagerStep() {
    return new StepBuilder("recalculateGradeManagerStep", jobRepository)
        .partitioner("recalculateGradeStep", new UserIdRangePartitioner(1, 100))
        .step(recalculateGradeStep())
        .gridSize(4)
        .taskExecutor(new SimpleAsyncTaskExecutor())
        .build();
  }

  @Bean
  public Step recalculateGradeStep() {
    return new StepBuilder("recalculateGradeStep", jobRepository)
        .tasklet(recalculateGradeTasklet(null, null))
        .build();
  }

  @Bean
  @StepScope
  public LoggingTasklet recalculateGradeTasklet(
      @Value("#{stepExecutionContext['minId']}") Long minId,
      @Value("#{stepExecutionContext['maxId']}") Long maxId) {
    return new LoggingTasklet("사용자 " + minId + "~" + maxId + " 등급 재계산");
  }
}
