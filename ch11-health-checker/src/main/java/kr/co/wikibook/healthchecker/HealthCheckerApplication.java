package kr.co.wikibook.healthchecker;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HealthCheckerApplication {
  static void main(String[] args) {
    SpringApplication.run(HealthCheckerApplication.class, args);
  }

  @Bean
  public ExecutionContextSerializer executionContextSerializer() {
    return new JacksonExecutionContextStringSerializer();
  }
}
