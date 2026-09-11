package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.time.LocalDate;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig(TestDbConfig.class)
@Transactional
class DeleteOldAccessLogTaskletTest {

  @Autowired
  DataSource dataSource;

  @DisplayName("시작일이 종료일보다 늦으면 태스클릿을 만들 때 예외를 던진다")
  @Test
  void invalidPeriod() { // <1>
    var startDay = LocalDate.of(2025, 4, 3);
    var endDay = LocalDate.of(2025, 4, 2);

    assertThatIllegalArgumentException().isThrownBy(() ->
        new DeleteOldAccessLogTasklet(this.dataSource, startDay, endDay)
    );
  }

  @DisplayName("기간에 해당하는 로그를 여러 번에 나눠 모두 지운다")
  @Test
  void deleteAccessLogs() {
    // given
    var jdbc = new NamedParameterJdbcTemplate(this.dataSource);
    var log1 = new AccessLog(Instant.parse("2025-04-01T11:14:16Z"), "192.168.0.1", "benelog");
    jdbc.update(AccessLogSql.INSERT, new BeanPropertySqlParameterSource(log1));
    var log2 = new AccessLog(Instant.parse("2025-04-02T11:14:16Z"), "192.168.0.1", "benelog");
    jdbc.update(AccessLogSql.INSERT, new BeanPropertySqlParameterSource(log2));

    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    var startDay = LocalDate.of(2025, 4, 1);
    var endDay = LocalDate.of(2025, 4, 2);

    // when
    var task = new DeleteOldAccessLogTasklet(this.dataSource, startDay, endDay);
    RepeatStatus status = RepeatStatus.CONTINUABLE;
    while (status != RepeatStatus.FINISHED) {
      status = task.execute(stepContribution, chunkContext);
    }

    // then
    assertThat(stepContribution.getWriteCount()).isEqualTo(2);
  }
}
