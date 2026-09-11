package kr.co.wikibook.batch.webadmin;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.support.TaskExecutorJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.core.task.SyncTaskExecutor;

public class JobTestSupports {
  public static JobOperatorTestUtils getJobOperatorTestUtils(Job job, JobRepository jobRepository) {
    var operator = new TaskExecutorJobOperator();
    operator.setJobRepository(jobRepository);
    operator.setTaskExecutor(new SyncTaskExecutor()); // <1>

    var testUtils = new JobOperatorTestUtils(operator, jobRepository); // <2>
    testUtils.setJob(job);
    return testUtils;
  }
}
