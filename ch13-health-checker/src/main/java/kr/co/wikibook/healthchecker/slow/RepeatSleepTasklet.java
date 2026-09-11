package kr.co.wikibook.healthchecker.slow;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class RepeatSleepTasklet implements Tasklet {

  private final Logger log = LoggerFactory.getLogger(RepeatSleepTasklet.class);
  private static final String COUNT_KEY = "repeatCount";

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws InterruptedException {
    TimeUnit.SECONDS.sleep(1);
    StepExecution stepExecution = contribution.getStepExecution();
    ExecutionContext executionContext = stepExecution.getExecutionContext();
    int count = executionContext.getInt(COUNT_KEY, 0) + 1;
    executionContext.putInt(COUNT_KEY, count);
    log.info("repeat count : {}", count);

    JobParameters jobParameters = stepExecution.getJobParameters();
    long limit = jobParameters.getLong("limit", 1L);
    if (count > limit) {
      stepExecution.setTerminateOnly();
    }
    return RepeatStatus.CONTINUABLE;
  }
}
