package kr.co.wikibook.batch.memo.etl;

import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class MemoComponents {

  public static FlatFileItemReader<String> memoFileReader(Resource resource) {
    return new FlatFileItemReaderBuilder<String>()
        .name("memoFileReader")
        .lineMapper(new PassThroughLineMapper())
        .resource(resource)
        .build();
  }

  public static JdbcBatchItemWriter<String> memoDbWriter(DataSource dataSource) {
    var writer = new JdbcBatchItemWriterBuilder<String>()
        .dataSource(dataSource)
        .sql("INSERT INTO memo(message) VALUES(?)")
        .itemPreparedStatementSetter((String message, PreparedStatement ps) ->
            ps.setString(1, message)
        )
        .build();
    return afterPropertiesSet(writer);
  }

  private static <T extends InitializingBean> T afterPropertiesSet(T bean) {
    try {
      bean.afterPropertiesSet();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    return bean;
  }
}
