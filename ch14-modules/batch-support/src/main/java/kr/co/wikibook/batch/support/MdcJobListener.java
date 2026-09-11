package kr.co.wikibook.batch.support;

import org.slf4j.MDC;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

public class MdcJobListener implements JobExecutionListener {

  @Override
  public void beforeJob(JobExecution jobExecution) {
    MDC.put("jobName", jobExecution.getJobInstance().getJobName());
    MDC.put("jobExecutionId", String.valueOf(jobExecution.getId()));
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    MDC.remove("jobName");
    MDC.remove("jobExecutionId");
  }
}
