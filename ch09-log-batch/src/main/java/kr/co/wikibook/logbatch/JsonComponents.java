package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.infrastructure.item.json.JacksonJsonObjectReader;
import org.springframework.batch.infrastructure.item.json.JsonFileItemWriter;
import org.springframework.batch.infrastructure.item.json.JsonItemReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

public class JsonComponents {

  public static JsonItemReader<AccessLog> buildJsonItemReader(Resource resource) {
    var objectReader = new JacksonJsonObjectReader<>(AccessLog.class);
    return new JsonItemReader<>(resource, objectReader);
  }

  public static JsonFileItemWriter<AccessLog> buildJsonItemWriter(WritableResource resource) {
    var objectMarshaller = new JacksonJsonObjectMarshaller<AccessLog>();
    return Configs.afterPropertiesSet(
        new JsonFileItemWriter<>(resource, objectMarshaller)
    );
  }
}
