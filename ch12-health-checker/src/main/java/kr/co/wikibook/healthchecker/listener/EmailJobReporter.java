package kr.co.wikibook.healthchecker.listener;

import java.util.List;
import kr.co.wikibook.healthchecker.util.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailJobReporter implements JobExecutionListener {

  private static final String MAIL_SENDER = "benelog@gmail.com"; // <1>
  private final Logger logger = LoggerFactory.getLogger(EmailJobReporter.class);

  private final JavaMailSender mailSender; // <2>
  private final List<String> receivers; // <3>
  private final boolean skipOnSuccess; // <4>

  public EmailJobReporter(JavaMailSender mailSender, List<String> receivers,
      boolean skipOnSuccess) { // <5>
    this.mailSender = mailSender;
    this.receivers = receivers;
    this.skipOnSuccess = skipOnSuccess;
  }

  @Override
  public void afterJob(JobExecution jobExec) {
    String jobName = jobExec.getJobInstance().getJobName();

    if (skipOnSuccess && jobExec.getStatus() == BatchStatus.COMPLETED) { // <6>
      logger.info("Skipped email report : {}", jobName);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(MAIL_SENDER);
    message.setTo(receivers.toArray(new String[receivers.size()]));
    message.setSubject(toSubject(jobExec, jobName));
    message.setText(toText(jobExec));
    mailSender.send(message);
  }


  private String toSubject(JobExecution jobExec, String jobName) { // <7>
    String jobDuration = Times.getReadableDuration(jobExec.getStartTime(), jobExec.getEndTime());
    return String.format("%s : %s (%s)", jobName, jobExec.getStatus(), jobDuration);
  }

  private String toText(JobExecution jobExec) { // <8>
    var text = new StringBuilder();
    for (StepExecution stepExec : jobExec.getStepExecutions()) {
      String stepDuration = Times.getReadableDuration(
          stepExec.getStartTime(),
          stepExec.getEndTime()
      );
      text.append(String.format("stepName: %s (%s)\n ", stepExec.getStepName(), stepDuration));
      text.append("Exceptions : " + stepExec.getFailureExceptions() + "\n");
      text.append("-------------\n");
    }
    return text.toString();
  }
}