package kr.co.wikibook.healthchecker.url;

import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import kr.co.wikibook.healthchecker.util.Configs;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.retry.RetryPolicy;


@Configuration
public class CheckUrlJobConfig {
  static final String INPUT_FILE_PARAM = "urlListFile";
  static final String OUTPUT_FILE_PATH = "./status.csv";
  private static final String INPUT_FILE_PARAM_EXP = "#{jobParameters['" + INPUT_FILE_PARAM + "']}"; // <1>
  private final JobRepository jobRepository;

  public CheckUrlJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job checkUrlJob() {
    var validator = new DefaultJobParametersValidator();
    validator.setRequiredKeys(new String[]{INPUT_FILE_PARAM});
    return new JobBuilder("checkUrlJob", jobRepository)
        .preventRestart()
        .validator(validator)
        .start(logResourceMetaStep())
        .next(checkUrlStep())
        .build();
  }

  @Bean
  public Step logResourceMetaStep() {
    return new StepBuilder("logResourceMetaStep", jobRepository)
        .allowStartIfComplete(true)
        .startLimit(5)
        .tasklet(logResourceMetaTasklet(null))
        .build();
  }

  @Bean
  public Step checkUrlStep() {

    var retryPolicy = RetryPolicy.builder()
        .maxRetries(3)
        .includes(HttpConnectTimeoutException.class)
        .delay(Duration.ofMillis(100L))
        .multiplier(2.0d)
        .maxDelay(Duration.ofMillis(600L))
        .build();

    return new StepBuilder("checkUrlStep", jobRepository)
        .<String, ResponseStatus>chunk(2)
        .reader(urlFileReader(null))
        .processor(callUrlProcessor(null))
        .writer(buildResponseStatusFileWriter())
        .faultTolerant()
        .skip(IllegalArgumentException.class)
        .skipLimit(2)
        .retryPolicy(retryPolicy)
        .build();
  }

  @Bean
  @StepScope
  public Tasklet logResourceMetaTasklet(@Value(INPUT_FILE_PARAM_EXP) FileSystemResource urlListFile) {
    var task = new LogResourceMetaTask(urlListFile);
    return new CallableTaskletAdapter(task);
  }

  @Bean
  @StepScope
  public FlatFileItemReader<String> urlFileReader(
      @Value(INPUT_FILE_PARAM_EXP) FileSystemResource urlListFile) {
    return new FlatFileItemReaderBuilder<String>()
        .name("urlFileReader")
        .resource(urlListFile)
        .lineMapper(new PassThroughLineMapper()) // <4>
        .build();
  }

  @Bean
  public CallUrlProcessor callUrlProcessor(@Value("${request.timeout}") Duration requestTimeout) {
    return new CallUrlProcessor(requestTimeout);
  }
  private FlatFileItemWriter<ResponseStatus> buildResponseStatusFileWriter() { // <6>
    var outputFile = new FileSystemResource(OUTPUT_FILE_PATH);
    var writer =  new FlatFileItemWriterBuilder<ResponseStatus>()
        .name("responseStatusFileWriter")
        .resource(outputFile)
        .delimited()
        .fieldExtractor(item -> new Object[]{
            item.statusCode(), item.responseTimeMillis() + "ms", item.url() // <7>
        })
        .build();
    return Configs.afterPropertiesSet(writer); // <8>
  }
}
