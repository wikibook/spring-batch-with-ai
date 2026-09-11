package kr.co.wikibook.logbatch;

import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

public class AccessLogCsvToDbTask implements CommandLineRunner {
  private final AccessLogCsvReader reader;
  private final AccessLogDbWriter writer;
  private final int chunkSize;
  private final Logger log = LoggerFactory.getLogger(AccessLogCsvToDbTask.class);

  public AccessLogCsvToDbTask(AccessLogCsvReader reader, AccessLogDbWriter writer, int chunkSize) {
    this.reader = reader;
    this.writer = writer;
    this.chunkSize = chunkSize;
  }

  @Override
  public void run(String... args) throws Exception {
    int totalItems = 0;
    this.reader.open();
    var chunk = new LinkedList<AccessLog>();

    while (true) {
      AccessLog item = this.reader.read();

      if (item == null) {
        if (!chunk.isEmpty()) {
          this.writer.write(chunk);
        }
        break;
      }

      totalItems++;
      chunk.add(item);
      if (chunk.size() == this.chunkSize) {
        this.writer.write(chunk);
        chunk.clear();
      }
    }

    this.reader.close();
    log.info("{}개의 항목을 CSV -> DB", totalItems);
  }
}
