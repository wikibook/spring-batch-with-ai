package kr.co.wikibook.batch.hello.job;

import kr.co.wikibook.batch.hello.tasklet.HelloTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;

public class Hello2JobConfig {

  public static final String JOB_NAME = "hello2Job";

  @Bean
  public Job hello2Job(JobRepository jobRepository) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(hello2Step(jobRepository))
        .build();
  }

  @Bean
  public Step hello2Step(JobRepository jobRepository) {
    var tasklet = new HelloTasklet();
    return new StepBuilder("hello2Step", jobRepository)
        .tasklet(tasklet)
        .build();
  }
}
