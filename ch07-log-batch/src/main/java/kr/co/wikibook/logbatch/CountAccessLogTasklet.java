package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CountAccessLogTasklet implements Tasklet {
  private final JdbcOperations jdbc;

  public CountAccessLogTasklet(DataSource dataSource) {
    this.jdbc = new JdbcTemplate(dataSource);
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    long count = jdbc.queryForObject("SELECT COUNT(1) FROM access_log", Long.class);

    StepExecution stepExecution = contribution.getStepExecution();

    ExecutionContext executionContext = stepExecution.getExecutionContext();
    executionContext.put("count", count);
    return RepeatStatus.FINISHED;
  }
}
