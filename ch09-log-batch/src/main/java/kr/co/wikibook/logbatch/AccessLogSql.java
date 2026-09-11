package kr.co.wikibook.logbatch;

public class AccessLogSql {
  public static final String INSERT = """
      INSERT INTO access_log(access_date_time, ip, username)
      VALUES (:accessDateTime, :ip, :username)
      """;

  public static final String COUNT_GROUP_BY_USERNAME = """
      SELECT username, COUNT(1) AS access_count
      FROM access_log
      WHERE  access_date_time BETWEEN ? AND ?
      GROUP BY username
      """;
}
