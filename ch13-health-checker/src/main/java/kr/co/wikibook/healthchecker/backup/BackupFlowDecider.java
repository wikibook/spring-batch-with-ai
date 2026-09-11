package kr.co.wikibook.healthchecker.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

public class BackupFlowDecider implements JobExecutionDecider {
  private final Logger logger = LoggerFactory.getLogger(BackupFlowDecider.class);

  @Override
  public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long sourceSize = executionContext.getLong("sourceSize", 0L);
    long usableSpace = executionContext.getLong("usableSpace", 0L);
    int executionCount = executionContext.getInt("executionCount", 0);

    if (sourceSize >= usableSpace) {
      logger.warn("sourceSize={} bytes, usableSpace={} bytes", sourceSize, usableSpace);
      if (executionCount > 1) {
        return FlowExecutionStatus.FAILED;
      }
      return new FlowExecutionStatus("RETRY");
    }
    return FlowExecutionStatus.COMPLETED;
  }
}
