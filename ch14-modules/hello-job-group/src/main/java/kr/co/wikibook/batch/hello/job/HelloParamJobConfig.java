package kr.co.wikibook.batch.hello.job;

import java.time.LocalDate;
import kr.co.wikibook.batch.hello.tasklet.HelloDate1Tasklet;
import kr.co.wikibook.batch.hello.tasklet.HelloDate2Tasklet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class HelloParamJobConfig {
  @Bean
  public Job helloParamJob(JobRepository jobRepository) {

    Step helloDate1Step = new StepBuilder("helloDate1Step", jobRepository)
        .tasklet(new HelloDate1Tasklet())
        .build();

    Step helloDate2Step = new StepBuilder("helloDate2Step", jobRepository)
        .tasklet(helloDate2Tasklet(null))
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
