package kr.co.wikibook.logbatch;

import java.io.IOException;
import java.util.Map;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class CheckDiskSpaceTasklet implements Tasklet {

  private final DiskSpaceMeter diskSpaceMeter = new DiskSpaceMeter();

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws IOException {
    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    String directory = (String) jobParameters.get("directory");
    long minUsablePercentage = (long) jobParameters.get("minUsablePercentage");

    int usablePercentage = diskSpaceMeter.getUsablePercentage(directory);

    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
    jobExecutionContext.putLong("usablePercentage", usablePercentage);

    if (usablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + usablePercentage + "% 사용 가능");
    }
    return RepeatStatus.FINISHED;
  }
}
