package kr.co.wikibook.healthchecker.slow;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlowJobConfig {
  @Bean
  public Job slowJob(JobRepository jobRepository) {
    var repeatSleepStep = new StepBuilder("repeatSleepStep", jobRepository)
        .tasklet(new RepeatSleepTasklet())
        .build();

    return new JobBuilder("slowJob", jobRepository)
        .start(repeatSleepStep)
        .build();
  }
}
