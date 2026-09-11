package kr.co.wikibook.logbatch;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CheckDiskSpaceTask implements CommandLineRunner {

  private final DiskSpaceMeter diskSpaceMeter = new DiskSpaceMeter();
  private final NotificationService notificationService;
  private final int minUsablePercentage;

  public CheckDiskSpaceTask(
      NotificationService notificationService,
      @Value("${disk.min-usable-percentage:10}") int minUsablePercentage) {
    this.notificationService = notificationService;
    this.minUsablePercentage = minUsablePercentage;
  }

  @Override
  public void run(String... args) throws IOException {
    if (args.length < 1) {
      return;
    }
    String directory = args[0];
    int usablePercentage = diskSpaceMeter.getUsablePercentage(directory);
    this.notificationService.send("남은 용량 " + usablePercentage + "%");
    if (usablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + usablePercentage + "% 사용 가능");
    }
  }
}
