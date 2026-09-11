package kr.co.wikibook.logbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class RepeatTasklet implements Tasklet {
  private final Logger log = LoggerFactory.getLogger(RepeatTasklet.class);
  private int count = 0;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    count++;
    log.info("count : {}", count);
    contribution.incrementWriteCount(1);
    return RepeatStatus.continueIf(count < 3);
  }
}
