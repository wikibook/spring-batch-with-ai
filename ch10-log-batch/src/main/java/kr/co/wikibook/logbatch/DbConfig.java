package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DbConfig {
  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource mainDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public PlatformTransactionManager mainTransactionManager(DataSource mainDataSource) {
    return new JdbcTransactionManager(mainDataSource);
  }

  @Bean
  @BatchDataSource
  @ConfigurationProperties(prefix = "job-db")
  public DataSource jobDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  @Primary
  public PlatformTransactionManager jobDbTransactionManager(@Qualifier("jobDataSource") DataSource jobDataSource) {
    return new JdbcTransactionManager(jobDataSource);
  }
}
