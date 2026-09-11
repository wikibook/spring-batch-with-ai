package kr.co.wikibook.logbatch;

import kr.co.wikibook.logbatch.atom.AtomEntry;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class AtomEntryProcessor implements ItemProcessor<AtomEntry, BlogPost> {
  @Override
  public BlogPost process(AtomEntry entry) {
    return new BlogPost(
        entry.getTitle(),
        entry.getLink().getHref(),
        entry.getUpdated()
    );
  }
}
