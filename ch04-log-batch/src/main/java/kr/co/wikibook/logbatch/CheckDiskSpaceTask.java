package kr.co.wikibook.logbatch;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;
import org.springframework.boot.CommandLineRunner;

//@Component
public class CheckDiskSpaceTask implements CommandLineRunner {

  private final DiskSpaceMeter diskSpaceMeter = new DiskSpaceMeter();
  private final Logger logger;

  public CheckDiskSpaceTask(ILoggerFactory loggerFactory) {
    this.logger = loggerFactory.getLogger(CheckDiskSpaceTask.class.getName()); // <1>
  }

  @Override
  public void run(String... args) throws IOException {
    if (args.length < 2) {
      return;
    }
    String directory = args[0];
    int minUsablePercentage = Integer.parseInt(args[1]);
    int usablePercentage = diskSpaceMeter.getUsablePercentage(directory);
    logger.info("남은 용량 {}%", usablePercentage);
    if (usablePercentage < minUsablePercentage) {
      throw new IllegalStateException("디스크 용량이 기대치보다 작습니다 : " + usablePercentage + "% 사용 가능");
    }
  }
}
