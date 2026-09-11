package kr.co.wikibook.logbatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloJobConfig {

  @Bean
  public Job helloJob(JobRepository jobRepository) {
    Step helloStep = new StepBuilder("helloStep", jobRepository)
        .tasklet(new HelloTasklet())
        .build();

    Step repeatStep = new StepBuilder("repeatStep", jobRepository)
        .tasklet(new RepeatTasklet())
        .build();

    return new JobBuilder("helloJob", jobRepository)
        .start(helloStep)
        .next(repeatStep)
        .build();
  }
}
