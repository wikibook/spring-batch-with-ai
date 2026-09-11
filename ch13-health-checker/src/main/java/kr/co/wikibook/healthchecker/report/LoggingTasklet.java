package kr.co.wikibook.healthchecker.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class LoggingTasklet implements Tasklet {
  private final Logger logger = LoggerFactory.getLogger(LoggingTasklet.class);
  private final String message;

  public LoggingTasklet(String message) {
    this.message = message;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    logger.info(this.message);
    return RepeatStatus.FINISHED;
  }
}
