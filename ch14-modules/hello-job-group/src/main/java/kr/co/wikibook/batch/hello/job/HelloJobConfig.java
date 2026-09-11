package kr.co.wikibook.batch.hello.job;

import kr.co.wikibook.batch.hello.tasklet.HelloTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class HelloJobConfig {
  @Bean
  public Job helloJob(JobRepository jobRepository, JdbcTransactionManager jdbcTransactionManager) {

    return new JobBuilder("helloJob", jobRepository)
        .start(helloStep(jobRepository))
        .build();
  }

  @Bean
  public Step helloStep(JobRepository jobRepository) {
    var tasklet = new HelloTasklet();
    return new StepBuilder("helloStep", jobRepository)
        .tasklet(tasklet)
        .build();
  }
}
