package kr.co.wikibook.logbatch;

import javax.sql.DataSource;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AccessLogDbWriter implements ItemWriter<AccessLog> {
  private final NamedParameterJdbcTemplate jdbc;

  public AccessLogDbWriter(DataSource dataSource) {
    this.jdbc = new NamedParameterJdbcTemplate(dataSource);
  }

  @Override
  public void write(Chunk<? extends AccessLog> chunk) {
    BeanPropertySqlParameterSource[] params = chunk.getItems().stream()
        .map(BeanPropertySqlParameterSource::new)
        .toArray(BeanPropertySqlParameterSource[]::new);
    this.jdbc.batchUpdate(AccessLogSql.INSERT, params);
  }
}
