package kr.co.wikibook.logbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Isolation;

@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository(
    dataSourceRef = "jobDataSource",
    transactionManagerRef = "jobDbTransactionManager",
    executionContextSerializerRef = "jacksonSerializer",
    isolationLevelForCreate = Isolation.REPEATABLE_READ
)
public class LogBatchApplication {
  static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(LogBatchApplication.class, args);
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }

  @Bean
  public ExitCodeGenerator codeGenerator() {
    return () -> 3;
  }

  @Bean
  public ExecutionContextSerializer jacksonSerializer() {
    return new JacksonExecutionContextStringSerializer();
  }
}
