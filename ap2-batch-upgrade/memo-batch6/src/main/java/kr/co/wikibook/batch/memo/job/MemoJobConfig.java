package kr.co.wikibook.batch.memo.job;

import javax.sql.DataSource;
import kr.co.wikibook.batch.memo.etl.MemoComponents;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MemoJobConfig {

  @Bean
  public Job memoJob(JobRepository jobRepository, Step memoStep) {
    return new JobBuilder("memoJob", jobRepository)
        .start(memoStep)
        .build();
  }

  @Bean
  @JobScope
  public Step memoStep(
      JobRepository jobRepository,
      @Value("#{jobParameters['memoFile']}") Resource memoFile,
      DataSource mainDataSource,
      PlatformTransactionManager mainTransactionManager
  ) {
    return new StepBuilder("memoStep", jobRepository)
        .<String, String>chunk(10)
        .transactionManager(mainTransactionManager)
        .reader(MemoComponents.memoFileReader(memoFile))
        .writer(MemoComponents.memoDbWriter(mainDataSource))
        .build();
  }
}
