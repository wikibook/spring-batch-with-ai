package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckStatusJobConfig {

  @Bean
  public Job checkStatusJob(JobRepository jobRepository, DataSource dataSource) {
    var promotionListener = new ExecutionContextPromotionListener();
    promotionListener.setKeys(new String[]{"count"});

    Step countAccessLogStep = new StepBuilder("countAccessLogStep", jobRepository)
        .tasklet(new CountAccessLogTasklet(dataSource))
        .listener(promotionListener)
        .build();

    Step checkDiskSpaceStep = new StepBuilder("checkDiskSpaceStep", jobRepository)
        .tasklet(new CheckDiskSpaceTasklet())
        .build();

    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{"directory", "minUsablePercentage"}); // <1>

    Step logDiskSpaceTaskStep = new StepBuilder("logDiskSpaceStep", jobRepository)
        .tasklet(logDiskSpaceTasklet(0L))
        .build();

    return new JobBuilder("checkStatusJob", jobRepository)
        .validator(validator)
        .start(countAccessLogStep)
        .next(checkDiskSpaceStep)
        .next(logDiskSpaceTaskStep)
        .build();
  }

  @Bean
  @StepScope
  public Tasklet logDiskSpaceTasklet(
      @Value("#{jobExecutionContext['usablePercentage']}") long usablePercentage
  ) {
    var logDiskSpaceTask = new LogDiskSpaceTask(usablePercentage);
    return new CallableTaskletAdapter(logDiskSpaceTask); // <3>
  }
}
