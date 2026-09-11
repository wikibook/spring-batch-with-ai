package kr.co.wikibook.logbatch;

import java.time.LocalDate;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloParamJobConfig {
  public static final LocalDate INJECTED = null;

  @Bean
  public Job helloParamJob(JobRepository jobRepository) {
    Step helloDate1Step = new StepBuilder("helloDate1Step", jobRepository)
        .tasklet(new HelloDate1Tasklet())
        .build();

    Step helloDate2Step = new StepBuilder("helloDate2Step", jobRepository)
        .tasklet(helloDate2Tasklet(INJECTED))
        .build();
    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{"helloDate"});

    return new JobBuilder("helloParamJob", jobRepository)
        .validator(validator)
        .start(helloDate1Step)
        .next(helloDate2Step)
        .build();
  }

  @Bean
  @StepScope
  public HelloDate2Tasklet helloDate2Tasklet(
      @Value("#{jobParameters['helloDate']}") LocalDate date) {
    return new HelloDate2Tasklet(date);
  }
}
