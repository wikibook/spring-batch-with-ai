package kr.co.wikibook.logbatch;

import java.io.IOException;
import org.springframework.batch.infrastructure.item.file.ResourcesItemReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ResourcesItemReaderBuilder {
  private String locationPattern;

  public ResourcesItemReaderBuilder locationPattern(String locationPattern) {
    this.locationPattern = locationPattern;
    return this;
  }

  public ResourcesItemReader build() {
    var resourcePatternResolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = null;
    try {
      resources = resourcePatternResolver.getResources(locationPattern);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    var reader = new ResourcesItemReader();
    reader.setResources(resources);
    return reader;
  }
}
