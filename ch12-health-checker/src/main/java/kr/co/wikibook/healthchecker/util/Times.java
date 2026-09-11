package kr.co.wikibook.healthchecker.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Times {
  public static String getReadableDuration(LocalDateTime from, LocalDateTime to) {
    long durationSeconds = from.until(to, ChronoUnit.SECONDS);
    long hours = durationSeconds / 60 / 60;
    long leftSeconds = durationSeconds - hours * 60 * 60;
    long minutes = leftSeconds / 60;
    long seconds = leftSeconds - minutes * 60;
    return String.format("%d:%02d:%02d", hours, minutes, seconds);
  }
}
