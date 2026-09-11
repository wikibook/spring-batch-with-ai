package kr.co.wikibook.logbatch;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloChunkJobConfig {

  public static final String JOB_NAME = "helloChunkJob";

  @Bean
  public Job helloChunkJob(JobRepository jobRepository) {

    return new JobBuilder(JOB_NAME, jobRepository)
        .start(new StepBuilder("printSequence", jobRepository)
            .<Integer, Integer>chunk(3)
            .reader(sequenceReader(1, 10))
            .processor(plus10Processor())
            .writer(consoleWriter())
            .stream(new HelloItemStream())
            .build())
        .build();
  }

  ItemReader<Integer> sequenceReader(int from, int to) {
    IntStream itemRange = IntStream.range(from, to + 1);
    PrimitiveIterator.OfInt iterator = itemRange.iterator();
    return new IteratorItemReader<>(iterator);
  }

  ItemProcessor<Integer, Integer> plus10Processor() {
    return (item) -> item + 10;
  }

  ItemWriter<Integer> consoleWriter() {
    return System.out::println;
  }
}
