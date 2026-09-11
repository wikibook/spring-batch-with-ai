package kr.co.wikibook.healthchecker.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


class EmailJobReporterTest {

  @Test
  void skipSendEmail() {
    // given
    var jobInstance = new JobInstance(0L, "testJob");
    var jobExecution = new JobExecution(0L, jobInstance, new JobParameters());
    jobExecution.setStatus(BatchStatus.COMPLETED);

    boolean skipOnSuccess = true;
    JavaMailSender mailSender = mock(JavaMailSender.class);
    var reporter = new EmailJobReporter(mailSender, List.of(), skipOnSuccess);

    // when
    reporter.afterJob(jobExecution);

    // then
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail() throws MessagingException {
    var greenMail = new GreenMail(ServerSetup.SMTP.dynamicPort());
    greenMail.start();
    try {
      // given
      var mailSender = new JavaMailSenderImpl();
      mailSender.setHost("localhost");
      mailSender.setPort(greenMail.getSmtp().getPort());

      var jobInstance = new JobInstance(0L, "testJob");

      var jobExecution = new JobExecution(0L, jobInstance, new JobParameters());
      LocalDateTime startTime = LocalDateTime.parse("2026-07-22T16:02:00");
      LocalDateTime endTime = LocalDateTime.parse("2026-07-22T18:08:45");
      jobExecution.setStatus(BatchStatus.COMPLETED);
      jobExecution.setStartTime(startTime);
      jobExecution.setEndTime(endTime);

      var stepExecution = new StepExecution(0L, "testStep", jobExecution);
      stepExecution.setStartTime(startTime);
      stepExecution.setEndTime(endTime);
      jobExecution.addStepExecutions(List.of(stepExecution));

      var reporter = new EmailJobReporter(
          mailSender,
          List.of("benelog@naver.com"),
          false
      );

      // when
      reporter.afterJob(jobExecution);

      // then
      MimeMessage[] emails = greenMail.getReceivedMessages();
      assertThat(emails).hasSize(1);
      MimeMessage email = emails[0];
      assertThat(email.getSubject()).isEqualTo("testJob : COMPLETED (2:06:45)");
      assertThat(email.getHeader("To", null)).isEqualTo("benelog@naver.com");
    } finally {
      greenMail.stop();
    }
  }
}
