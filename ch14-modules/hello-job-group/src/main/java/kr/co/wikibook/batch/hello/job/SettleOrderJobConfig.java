package kr.co.wikibook.batch.hello.job;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.List;
import java.util.stream.IntStream;
import kr.co.wikibook.batch.support.MdcJobListener;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 주문 정산을 흉내 낸 잡.
 * 정산한 금액을 커스텀 메트릭으로 남기는 예제로, 마이크로미터의 전역 레지스트리에 등록한다.
 * admin-web처럼 부트로 실행하는 모듈에서는 자동 구성된 MeterRegistry가 전역 레지스트리에 연결되어 기록되고,
 * 부트를 쓰지 않는 CLI 모듈에서는 Metrics.addRegistry(...)로 실제 레지스트리를 연결해야 기록된다.
 */
@Configuration
public class SettleOrderJobConfig {
  private final Logger log = LoggerFactory.getLogger(SettleOrderJobConfig.class);

  @Bean
  public Job settleOrderJob(JobRepository jobRepository) {
    return new JobBuilder("settleOrderJob", jobRepository)
        .start(settleOrderStep(jobRepository))
        .listener(new MdcJobListener())
        .build();
  }

  @Bean
  public Step settleOrderStep(JobRepository jobRepository) {
    return new StepBuilder("settleOrderStep", jobRepository)
        .<Order, Order>chunk(10)
        .reader(orderReader())
        .writer(settlementWriter())
        .build();
  }

  @Bean
  @StepScope
  public ItemReader<Order> orderReader() {
    List<Order> orders = IntStream.rangeClosed(1, 100)
        .mapToObj(seq -> new Order("ORD-" + seq, seq * 1_000L))
        .toList();
    return new ListItemReader<>(orders);
  }

  ItemWriter<Order> settlementWriter() {
    Counter amountCounter = Counter.builder("batch.settlement.amount")
        .tag("batch.job", "settleOrderJob")
        .description("정산된 주문 금액 합계")
        .register(Metrics.globalRegistry);
    return chunk -> {
      for (Order order : chunk.getItems()) {
        // 실제 정산 처리는 생략
        amountCounter.increment(order.amount());
      }
      log.info("settled : {}", chunk.getItems());
    };
  }
}
