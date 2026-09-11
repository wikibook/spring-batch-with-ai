package kr.co.wikibook.logbatch;

import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemReader;
import org.springframework.batch.infrastructure.item.file.MultiResourceItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class AccessLogComponents {
  public static MultiResourceItemReader<AccessLog> buildMultiResourceItemReader(String locationPattern)
      throws Exception {
    var resourcePatternResolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resourcePatternResolver.getResources(locationPattern);
    FlatFileItemReader<AccessLog> delegator = buildDelegatorToRead();
    return new MultiResourceItemReaderBuilder<AccessLog>()
        .name("accessLogMultiFileReader")
        .resources(resources)
        .delegate(delegator)
        .build();
  }

  public static MultiResourceItemWriter<AccessLog> buildMultiResourceItemWriter(
      WritableResource resource,
      int itemsPerResource) throws Exception {

    FlatFileItemWriter<AccessLog> delegate = buildDelegateToWrite(resource);
    delegate.afterPropertiesSet();
    return new MultiResourceItemWriterBuilder<AccessLog>()
        .name("accessLogCsvMultiWriter")
        .resource(resource) // <1>
        .delegate(delegate) // <2>
        .itemCountLimitPerResource(itemsPerResource) // <3>
        .build();
  }

  private static FlatFileItemReader<AccessLog> buildDelegatorToRead() {
    return new FlatFileItemReaderBuilder<AccessLog>()
        .name("accessLogCsvReader")
        .lineTokenizer(new DelimitedLineTokenizer())
        .fieldSetMapper(new AccessLogFieldSetMapper())
        .build();
  }

  private static FlatFileItemWriter<AccessLog> buildDelegateToWrite(WritableResource resource) {
    return new FlatFileItemWriterBuilder<AccessLog>()
        .name("accessLogCsvWriter")
        .resource(resource)
        .delimited()
        .fieldExtractor((AccessLog item) -> new Object[]{ // <4>
            item.accessDateTime(),
            item.ip(),
            item.username()
        })
        .build();
  }
}
