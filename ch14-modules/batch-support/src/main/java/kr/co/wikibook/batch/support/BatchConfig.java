package kr.co.wikibook.batch.support;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository(
    dataSourceRef = "jobMetaDataSource",
    transactionManagerRef = "jobMetaTransactionManager"
)
@Import(JobService.class)
public class BatchConfig {

  private final Environment environment;

  public BatchConfig(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public DataSource jobMetaDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(environment.getProperty("job-db.driver-class-name"));
    hikariConfig.setJdbcUrl(environment.getProperty("job-db.jdbc-url"));
    hikariConfig.setUsername(environment.getProperty("job-db.username"));
    hikariConfig.setPassword(environment.getProperty("job-db.password"));
    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public PlatformTransactionManager jobMetaTransactionManager() {
    return new JdbcTransactionManager(jobMetaDataSource());
  }

  @Bean
  public DataSourceInitializer jobMetaDbInit(
      @Value("${job-db.schema-locations}") Resource[] schemaLocations
  ) {
    var populator = new ResourceDatabasePopulator();
    populator.addScripts(schemaLocations);
    populator.setContinueOnError(true);
    var initializer = new DataSourceInitializer();
    initializer.setDataSource(jobMetaDataSource());
    initializer.setDatabasePopulator(populator);
    return initializer;
  }

  @Bean
  public JobRegistry jobRegistry() {
    return new MapJobRegistry();
  }
}
