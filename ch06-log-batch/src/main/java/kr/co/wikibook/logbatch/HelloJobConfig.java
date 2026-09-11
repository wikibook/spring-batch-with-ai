package kr.co.wikibook.logbatch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Configuration
public class HelloJobConfig {

  @Bean
  public Job helloJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    var notSupported = new DefaultTransactionAttribute(Propagation.NOT_SUPPORTED.value());
    Step helloStep = new StepBuilder("helloStep", jobRepository)
        .tasklet(new HelloTasklet())
        .transactionAttribute(notSupported)
        .build();

    Step repeatStep = new StepBuilder("repeatStep", jobRepository)
        .tasklet(new RepeatTasklet())
        .transactionManager(transactionManager)
        .build();

    var incrementer = new RunIdIncrementer();
    incrementer.setKey("runId");
    return new JobBuilder("helloJob", jobRepository)
        .incrementer(incrementer)
        .start(helloStep)
        .next(repeatStep)
        .build();
  }
}
