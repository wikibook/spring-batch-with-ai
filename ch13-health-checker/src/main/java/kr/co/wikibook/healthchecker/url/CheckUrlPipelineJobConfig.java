package kr.co.wikibook.healthchecker.url;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import kr.co.wikibook.healthchecker.util.Configs;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.infrastructure.item.queue.BlockingQueueItemReader;
import org.springframework.batch.infrastructure.item.queue.BlockingQueueItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class CheckUrlPipelineJobConfig {

  static final String INPUT_FILE_PARAM = "urlListFile";

  private final JobRepository jobRepository;

  public CheckUrlPipelineJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public BlockingQueue<String> urlQueue() {
    return new ArrayBlockingQueue<>(100);
  }

  @Bean
  public Step urlProducerStep(BlockingQueue<String> urlQueue) {
    return new StepBuilder("urlProducerStep", this.jobRepository)
        .<String, String>chunk(10)
        .reader(urlFileReader(null))
        .writer(new BlockingQueueItemWriter<>(urlQueue))
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<String> urlFileReader(
      @Value("#{jobParameters['" + INPUT_FILE_PARAM + "']}") FileSystemResource urlListFile) {
    return new FlatFileItemReaderBuilder<String>()
        .name("urlFileReader")
        .resource(urlListFile)
        .lineMapper(new PassThroughLineMapper())
        .build();
  }

  @Bean
  public CallUrlProcessor callUrlProcessor(@Value("${request.timeout}") Duration requestTimeout) {
    return new CallUrlProcessor(requestTimeout);
  }

  @Bean
  public Step urlConsumerStep(BlockingQueue<String> urlQueue, CallUrlProcessor callUrlProcessor) {
    var queueReader = new BlockingQueueItemReader<>(urlQueue);
    queueReader.setTimeout(5, TimeUnit.SECONDS);
    return new StepBuilder("urlConsumerStep", this.jobRepository)
        .<String, ResponseStatus>chunk(10)
        .reader(queueReader)
        .processor(callUrlProcessor)
        .writer(responseStatusFileWriter())
        .build();
  }

  @Bean
  public Job checkUrlPipelineJob(Step urlProducerStep, Step urlConsumerStep) {
    Flow producerFlow = new FlowBuilder<SimpleFlow>("producerFlow")
        .start(urlProducerStep)
        .build();
    Flow consumerFlow = new FlowBuilder<SimpleFlow>("consumerFlow")
        .start(urlConsumerStep)
        .build();
    return new JobBuilder("checkUrlPipelineJob", this.jobRepository)
        .start(producerFlow)
        .split(new SimpleAsyncTaskExecutor())
        .add(consumerFlow)
        .end()
        .build();
  }

  private FlatFileItemWriter<ResponseStatus> responseStatusFileWriter() {
    var outputFile = new FileSystemResource("status-pipeline.csv");
    var writer = new FlatFileItemWriterBuilder<ResponseStatus>()
        .name("pipelineStatusWriter")
        .resource(outputFile)
        .delimited()
        .fieldExtractor(item -> new Object[]{
            item.statusCode(), item.responseTimeMillis() + "ms", item.url()
        })
        .build();
    return Configs.afterPropertiesSet(writer);
  }
}
