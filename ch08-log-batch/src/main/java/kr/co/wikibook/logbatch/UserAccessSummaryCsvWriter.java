package kr.co.wikibook.logbatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;
import org.springframework.core.io.Resource;

public class UserAccessSummaryCsvWriter implements ItemStreamWriter<UserAccessSummary> {
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private final Resource resource;
  private final UserAccessSummaryLineAggregator lineAggregator =
      new UserAccessSummaryLineAggregator();
  private BufferedWriter lineWriter;

  public UserAccessSummaryCsvWriter(Resource resource) {
    this.resource = resource;
  }

  @Override
  public void open(ExecutionContext context) throws ItemStreamException {
    try {
      this.lineWriter = Files.newBufferedWriter(Paths.get(resource.getURI()));
    } catch (IOException ex) {
      throw new ItemStreamException(ex);
    }
  }

  @Override
  public void write(Chunk<? extends UserAccessSummary> chunk) throws IOException {
    for (UserAccessSummary item : chunk) {
      this.lineWriter.write(lineAggregator.aggregate(item));
      this.lineWriter.write(LINE_SEPARATOR);
    }
  }

  @Override
  public void close() throws ItemStreamException {
    try {
      this.lineWriter.close();
    } catch (IOException ex) {
      throw new ItemStreamException(ex);
    }
  }
}
