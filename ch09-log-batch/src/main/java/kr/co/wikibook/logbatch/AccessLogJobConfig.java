package kr.co.wikibook.logbatch;

import java.nio.file.Path;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class AccessLogJobConfig {

  public static final String JOB_NAME = "accessLogJob";

  private final DataSource dataSource;
  private final JobRepository jobRepository;
  private final Path basePath;

  public AccessLogJobConfig(
      DataSource dataSource, JobRepository jobRepository,
      @Value("${base-path:./logs}") Path basePath) {
    this.dataSource = dataSource;
    this.jobRepository = jobRepository;
    this.basePath = basePath;
  }

  @Bean
  public Job accessLogJob() {
    return new JobBuilder(JOB_NAME, this.jobRepository)
        .start(this.csvToDbStep(null))
        .next(this.dbToCsvStep(null))
        .build();
  }

  @Bean
  public Step csvToDbStep(JdbcTransactionManager transactionManager) {
    ItemStreamReader<AccessLog> reader = this.accessLogCsvReader(null);
    var writer = new AccessLogDbWriter(this.dataSource);
    return new StepBuilder("accessLogCsvToDb", this.jobRepository)
        .<AccessLog, AccessLog>chunk(300)
        .transactionManager(transactionManager)
        .reader(reader)
        .processor(new AccessLogProcessor())
        .writer(writer)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<AccessLog> accessLogCsvReader(
      @Value("#{jobParameters['date']}") LocalDate date
  ) {
    var resource = new FileSystemResource(basePath.resolve(date + ".csv"));
    return new FlatFileItemReaderBuilder<AccessLog>()
        .name("accessLogCsvReader")
        .resource(resource)
        .lineTokenizer(new DelimitedLineTokenizer())
        .fieldSetMapper(new AccessLogFieldSetMapper())
        .build();
  }

  @Bean
  @JobScope
  public Step dbToCsvStep(
      @Value("#{jobParameters['date']}") LocalDate date
  ) {
    var resource = new FileSystemResource(basePath.resolve(date + "_summary.csv"));
    var reader = new UserAccessSummaryDbReader(this.dataSource, date);
    FlatFileItemWriter<UserAccessSummary> writer = UserAccessSummaryComponents.buildCsvWriter(resource);
    return new StepBuilder("userAccessSummaryDbToCsv", jobRepository)
        .<UserAccessSummary, UserAccessSummary>chunk(300)
        .reader(reader)
        .writer(writer)
        .build();
  }
}
