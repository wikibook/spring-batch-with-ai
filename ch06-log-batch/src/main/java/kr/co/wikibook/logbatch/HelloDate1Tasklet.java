package kr.co.wikibook.logbatch;

import java.time.LocalDate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class HelloDate1Tasklet implements Tasklet {
  private final Logger log = LoggerFactory.getLogger(HelloDate1Tasklet.class);

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    LocalDate helloDate = (LocalDate) jobParameters.get("helloDate");
    log.info("Hello {} ", helloDate);
    return RepeatStatus.FINISHED;
  }
}
