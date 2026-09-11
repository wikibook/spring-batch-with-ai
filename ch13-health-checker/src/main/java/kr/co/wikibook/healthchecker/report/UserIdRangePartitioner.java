package kr.co.wikibook.healthchecker.report;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

public class UserIdRangePartitioner implements Partitioner {
  private final long minId;
  private final long maxId;

  public UserIdRangePartitioner(long minId, long maxId) {
    this.minId = minId;
    this.maxId = maxId;
  }

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    long targetSize = (maxId - minId) / gridSize + 1;
    var partitions = new HashMap<String, ExecutionContext>();
    long start = minId;
    int number = 0;
    while (start <= maxId) {
      var context = new ExecutionContext();
      context.putLong("minId", start);
      context.putLong("maxId", Math.min(start + targetSize - 1, maxId));
      partitions.put("partition" + number, context);
      start += targetSize;
      number++;
    }
    return partitions;
  }
}
