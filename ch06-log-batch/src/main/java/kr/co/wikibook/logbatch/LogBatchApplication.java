package kr.co.wikibook.logbatch;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.batch.autoconfigure.BatchConversionServiceCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogBatchApplication {
  static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(LogBatchApplication.class, args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }

  @Bean
  public ExecutionContextSerializer executionContextSerializer() {
    return new JacksonExecutionContextStringSerializer();
  }

  @Bean
  public BatchConversionServiceCustomizer conversionServiceCustomizer() {
    return configurableConversionService -> {
      configurableConversionService.addConverter(new StringToColorConverter());
      configurableConversionService.addConverter(new ColorToStringConverter());
    };
  }
}
