package kr.co.wikibook.logbatch;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class LogBatchApplication {
  static void main(String[] args) {
    SpringApplication.run(LogBatchApplication.class, args);
  }

  @Bean
  @Profile("production")
  public NotificationService slackNotificationService(
      @Value("${notify.slack-webhook-url}") URI webhookUrl) {
    return new SlackNotificationService(webhookUrl);
  }

  @Bean
  @Profile("!production")
  public NotificationService loggingNotificationService() {
    return new LoggingService();
  }
}
