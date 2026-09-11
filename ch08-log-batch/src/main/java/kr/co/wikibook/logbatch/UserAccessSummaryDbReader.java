package kr.co.wikibook.logbatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import javax.sql.DataSource;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.jspecify.annotations.Nullable;

public class UserAccessSummaryDbReader implements ItemStreamReader<UserAccessSummary> {

  private final RowMapper<UserAccessSummary> rowMapper = new DataClassRowMapper<>(UserAccessSummary.class);

  private final DataSource dataSource;
  private final LocalDate date;
  private PreparedStatement statement;
  private Connection con;
  private ResultSet resultSet;
  private int rowCount = 0;

  public UserAccessSummaryDbReader(DataSource dataSource, LocalDate date) {
    this.dataSource = dataSource;
    this.date = date;
  }

  @Override
  public void open(ExecutionContext executionContext) {
    this.con = DataSourceUtils.getConnection(dataSource);
    try {
      this.statement = con.prepareStatement(AccessLogSql.COUNT_GROUP_BY_USERNAME, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
      Instant to = from.plus(1, ChronoUnit.DAYS);
      this.statement.setObject(1, from);
      this.statement.setObject(2, to);
      this.resultSet = statement.executeQuery();
    } catch (SQLException ex) {
      close();
      throw new RuntimeException(ex);
    }
  }

  @Nullable
  public UserAccessSummary read() throws SQLException {
    if (resultSet.next()) {
      UserAccessSummary item = this.rowMapper.mapRow(resultSet, rowCount);
      rowCount++;
      return item;
    }
    return null;
  }

  @Override
  public void close() {
    this.rowCount = 0;
    JdbcUtils.closeResultSet(this.resultSet);
    JdbcUtils.closeStatement(this.statement);
    DataSourceUtils.releaseConnection(this.con, this.dataSource);
  }
}