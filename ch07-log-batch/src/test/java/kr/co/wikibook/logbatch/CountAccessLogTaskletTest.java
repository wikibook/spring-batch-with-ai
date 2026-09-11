package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestDbConfig.class)
class CountAccessLogTaskletTest {

  @DisplayName("access_log 테이블의 건수를 세어 실행 컨텍스트에 저장한다")
  @Test
  void countAccessLog(@Autowired DataSource dataSource) throws Exception {
    // given
    var task = new CountAccessLogTasklet(dataSource);

    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));

    // when
    RepeatStatus repeatStatus = task.execute(stepContribution, chunkContext);

    // then
    assertThat(repeatStatus).isEqualTo(RepeatStatus.FINISHED);
    long count = stepExecution.getExecutionContext().getLong("count");
    assertThat(count).isGreaterThanOrEqualTo(0L);
  }
}
