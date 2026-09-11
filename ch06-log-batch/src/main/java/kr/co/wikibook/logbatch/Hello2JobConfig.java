package kr.co.wikibook.logbatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = Hello2JobConfig.JOB_NAME)
public class Hello2JobConfig {

  public static final String JOB_NAME = "hello2Job";


  @Bean
  public Job hello2Job(JobRepository jobRepository) {
    var tasklet = new HelloTasklet();
    Step helloStep = new StepBuilder("hello2Step", jobRepository)
        .tasklet(tasklet)
        .build();

    return new JobBuilder(JOB_NAME, jobRepository)
        .start(helloStep)
        .build();
  }
}
