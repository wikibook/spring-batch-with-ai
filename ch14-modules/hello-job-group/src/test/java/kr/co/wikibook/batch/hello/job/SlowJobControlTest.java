package kr.co.wikibook.batch.hello.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.UUID;
import kr.co.wikibook.batch.hello.HelloJobGroupRunner;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(HelloJobGroupRunner.class)
class SlowJobControlTest {

  @Autowired
  JobRegistry registry;

  @Autowired
  JobRepository jobRepository;

  @Test
  void stopAndRestart(@Autowired Job slowJob) throws Exception {
    JobOperator operator = asyncJobOperator();
    JobParameters params = new JobParametersBuilder()
        .addString("id", UUID.randomUUID().toString())
        .addLong("limit", 60L)
        .toJobParameters();

    JobExecution execution = operator.start(slowJob, params);
    awaitStepRunning(execution.getId());

    boolean stopping = operator.stop(execution);
    assertThat(stopping).isTrue();
    awaitStopped(execution.getId());

    JobExecution stopped = jobRepository.getJobExecution(execution.getId());
    JobExecution restarted = operator.restart(stopped);
    assertThat(restarted.getId()).isNotEqualTo(execution.getId());

    awaitStepRunning(restarted.getId());
    operator.stop(restarted);
    awaitStopped(restarted.getId());
  }

  private JobOperator asyncJobOperator() throws Exception {
    var operator = new TaskExecutorJobOperator();
    operator.setJobRepository(jobRepository);
    operator.setJobRegistry(registry);
    operator.setTaskExecutor(new SimpleAsyncTaskExecutor());
    operator.afterPropertiesSet();
    return operator;
  }

  private void awaitStepRunning(long executionId) throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      JobExecution execution = jobRepository.getJobExecution(executionId);
      boolean stepRunning = execution.getStepExecutions().stream()
          .anyMatch(step -> step.getStatus() == BatchStatus.STARTED);
      if (execution.getStatus() == BatchStatus.STARTED && stepRunning) {
        return;
      }
      Thread.sleep(100);
    }
    fail("스텝 실행이 시작되지 않았다. executionId=" + executionId);
  }

  private void awaitStopped(long executionId) throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      JobExecution execution = jobRepository.getJobExecution(executionId);
      boolean stepsDone = execution.getStepExecutions().stream()
          .noneMatch(step -> step.getStatus().isRunning());
      if (execution.getStatus() == BatchStatus.STOPPED && stepsDone) {
        return;
      }
      Thread.sleep(100);
    }
    fail("잡 실행이 멈추지 않았다. executionId=" + executionId);
  }
}
