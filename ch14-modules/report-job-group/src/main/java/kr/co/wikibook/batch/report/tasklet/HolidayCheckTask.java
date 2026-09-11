package kr.co.wikibook.batch.report.tasklet;

import java.time.LocalDate;
import java.util.concurrent.Callable;
import kr.co.wikibook.batch.report.util.Dates;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

public class HolidayCheckTask implements Callable<RepeatStatus> {

  private final LocalDate executionDay;

  public HolidayCheckTask(LocalDate executionDay) {
    this.executionDay = executionDay;
  }

  @Override
  public RepeatStatus call() {
    if (Dates.isHoliday(executionDay)) {
      throw new RuntimeException(executionDay + " is a holiday");
    }
    return RepeatStatus.FINISHED;
  }
}
