package kr.co.wikibook.logbatch.batchconfig;

import javax.sql.DataSource;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSourceScriptDatabaseInitializer;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchJdbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;

@Configuration
@EnableConfigurationProperties(BatchJdbcProperties.class)
public class JdbcBatchConfig extends JdbcDefaultBatchConfiguration {
  private final BatchJdbcProperties properties;
  private final PlatformTransactionManager transactionManager;

  private final DataSource dataSource;

  public JdbcBatchConfig(
      BatchJdbcProperties properties,
      @Qualifier("jobDbTransactionManager") PlatformTransactionManager transactionManager,
      @Qualifier("jobDataSource") DataSource dataSource) {
    this.properties = properties;
    this.transactionManager = transactionManager;
    this.dataSource = dataSource;
  }

  @Override
  protected DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  protected PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  @Override
  protected String getTablePrefix() {
    String tablePrefix = this.properties.getTablePrefix();
    return (tablePrefix != null) ? tablePrefix : super.getTablePrefix();
  }

  @Override
  protected Isolation getIsolationLevelForCreate() {
    Isolation isolation = this.properties.getIsolationLevelForCreate();
    return (isolation != null) ? isolation : super.getIsolationLevelForCreate();
  }

  @Override
  protected ExecutionContextSerializer getExecutionContextSerializer() {
    return new JacksonExecutionContextStringSerializer();
  }

  @Bean
  public DataSourceScriptDatabaseInitializer metaDbInit() {
    return new BatchDataSourceScriptDatabaseInitializer(dataSource, properties);
  }
}
