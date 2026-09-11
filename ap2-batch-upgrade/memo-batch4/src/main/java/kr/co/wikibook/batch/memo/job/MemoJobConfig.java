package kr.co.wikibook.batch.memo.job;

import javax.sql.DataSource;
import kr.co.wikibook.batch.memo.etl.MemoComponents;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class MemoJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  public MemoJobConfig(JobBuilderFactory jobBuilderFactory,
                       StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
  }

  @Bean
  public Job memoJob() {
    return this.jobBuilderFactory.get("memoJob")
        .start(memoStep(null, null))
        .build();
  }

  @Bean
  @JobScope
  public Step memoStep(
      @Value("#{jobParameters['memoFile']}") Resource memoFile,
      DataSource mainDataSource
  ) {
    return this.stepBuilderFactory.get("memoStep")
        .<String, String>chunk(10)
        .reader(MemoComponents.memoFileReader(memoFile))
        .writer(MemoComponents.memoDbWriter(mainDataSource))
        .build();
  }
}
