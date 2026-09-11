package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("spring.batch.job.enabled=false")
class AccessLogCsvReaderStepScopeTest {

  Logger logger = LoggerFactory.getLogger(this.getClass());

  @DisplayName("스텝 스코프 리더가 잡 파라미터의 날짜로 파일을 읽는다")
  @Test
  void read(@Autowired FlatFileItemReader<AccessLog> reader) throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLocalDate("date", LocalDate.of(2026, 7, 28))
        .toJobParameters();
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);

    StepScopeTestUtils.doInStepScope(stepExecution, () -> {
      reader.open(new ExecutionContext());
      int itemCount = 0;
      AccessLog item;
      while ((item = reader.read()) != null) {
        itemCount++;
        logger.info("{}", item);
      }
      reader.close();
      assertThat(itemCount).isEqualTo(3);
      return null;
    });
  }
}
