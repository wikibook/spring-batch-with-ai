package kr.co.wikibook.batch.support;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(
    objectName = "kr.co.wikibook.batch.support:type=JobService,name=jobService",
    description = "Job을 제어")
public class JobService {
  private final JobRegistry registry;
  private final JobOperator operator;
  private final JobRepository repository;
  private JobParametersConverter converter = new DefaultJobParametersConverter();

  public JobService(JobRegistry registry, JobOperator operator, JobRepository repository) {
    this.registry = registry;
    this.operator = operator;
    this.repository = repository;
  }

  public long start(String jobName, @Nullable Properties parameters) {
    try {
      Job job = registry.getJob(jobName);
      Properties params = (parameters != null) ? parameters : new Properties();
      JobParameters jobParameters = converter.getJobParameters(params);
      JobExecution execution = operator.start(job, jobParameters);
      return execution.getId();
    } catch (JobExecutionException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @ManagedOperation(description = "Job 이름으로 JobExecution을 중지")
  public void stopExecutions(String jobName) throws JobExecutionException {
    Set<JobExecution> runningExecutions = repository.findRunningJobExecutions(jobName);
    for (JobExecution execution : runningExecutions) {
      operator.stop(execution);
    }
  }

  @ManagedOperation(description = "실행 중인 모든 JobExecution을 중지")
  public void stopAllExecutions() throws JobExecutionException {
    Collection<String> jobNames = registry.getJobNames();
    for (String jobName : jobNames) {
      stopExecutions(jobName);
    }
  }
}
