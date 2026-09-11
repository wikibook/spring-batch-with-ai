package kr.co.wikibook.logbatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.core.io.WritableResource;

public class UserAccessSummaryCsvWriter {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private final WritableResource resource;
  private final UserAccessSummaryLineAggregator lineAggregator = new UserAccessSummaryLineAggregator();
  private BufferedWriter lineWriter;

  public UserAccessSummaryCsvWriter(WritableResource resource) {
    this.resource = resource;
  }

  public void open() throws IOException {
    this.lineWriter = Files.newBufferedWriter(Paths.get(resource.getURI()));
  }

  public void write(List<UserAccessSummary> items) throws IOException {
    for (UserAccessSummary item : items) {
      this.lineWriter.write(lineAggregator.aggregate(item));
      this.lineWriter.write(LINE_SEPARATOR);
    }
  }

  public void close() throws IOException {
    if (this.lineWriter != null) {
      this.lineWriter.close();
    }
  }
}
