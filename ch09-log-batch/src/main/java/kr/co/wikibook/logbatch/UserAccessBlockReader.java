package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.core.io.Resource;

public class UserAccessBlockReader implements ItemStreamReader<UserAccessSummary> {
  private final SingleItemPeekableItemReader<FieldSet> lines;

  public UserAccessBlockReader(Resource resource) {
    var lineReader = new FlatFileItemReaderBuilder<FieldSet>()
        .name("userAccessLineReader")
        .resource(resource)
        .lineTokenizer(new DelimitedLineTokenizer(";"))
        .fieldSetMapper(new PassThroughFieldSetMapper())
        .build();
    this.lines = new SingleItemPeekableItemReader<>(lineReader);
  }

  @Override
  public UserAccessSummary read() throws Exception {
    FieldSet userLine = lines.read();
    if (userLine == null) {
      return null;
    }
    if (!"USR".equals(userLine.readString(0))) {
      throw new IllegalStateException("레코드는 USR 줄로 시작해야 한다.");
    }
    String username = userLine.readString(1);
    int accessCount = 0;
    while (isAccessLine(lines.peek())) {
      lines.read();
      accessCount++;
    }
    return new UserAccessSummary(username, accessCount);
  }

  private boolean isAccessLine(FieldSet line) {
    return line != null && "ACC".equals(line.readString(0));
  }

  @Override
  public void open(ExecutionContext executionContext) {
    lines.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) {
    lines.update(executionContext);
  }

  @Override
  public void close() {
    lines.close();
  }
}
