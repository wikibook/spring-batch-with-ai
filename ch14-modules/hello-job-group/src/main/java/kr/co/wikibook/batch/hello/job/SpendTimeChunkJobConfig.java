package kr.co.wikibook.batch.hello.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import kr.co.wikibook.batch.support.MdcJobListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpendTimeChunkJobConfig {
  private final Logger log = LoggerFactory.getLogger(SpendTimeChunkJobConfig.class);

  @Bean
  public Job spendTimeChunkJob(JobRepository jobRepository) {
    return new JobBuilder("spendTimeChunkJob", jobRepository)
        .start(sleepMilliSecondsStep(jobRepository))
        .listener(new MdcJobListener())
        .build();
  }

  @Bean
  public Step sleepMilliSecondsStep(JobRepository jobRepository) {
    return new StepBuilder("sleepMilliSecondsStep", jobRepository)
        .<Integer, Integer>chunk(10)
        .reader(millSecondsReader())
        .writer(milliSecondsSleeper())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<Integer> millSecondsReader() {
    IntStream weightRange = IntStream.range(1, 300);
    List<Integer> weights = new ArrayList<>(weightRange.boxed().toList());
    Collections.shuffle(weights);
    return new ListItemReader<>(weights);
  }

  ItemWriter<Integer> milliSecondsSleeper() {
    return chunk -> {
      for(var item : chunk.getItems()) {
        TimeUnit.MILLISECONDS.sleep(item.longValue());
      }
      log.info("written : {}", chunk);
    };
  }
}
