package kr.co.wikibook.logbatch;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class AccessLogDbWriter {

  private final NamedParameterJdbcOperations jdbc;

  public AccessLogDbWriter(DataSource dataSource) {
    this.jdbc = new NamedParameterJdbcTemplate(dataSource);
  }

  public void write(List<AccessLog> items) {
    BeanPropertySqlParameterSource[] params = items.stream()
        .map(BeanPropertySqlParameterSource::new)
        .toArray(BeanPropertySqlParameterSource[]::new);
    this.jdbc.batchUpdate(AccessLogSql.INSERT, params);
  }
}