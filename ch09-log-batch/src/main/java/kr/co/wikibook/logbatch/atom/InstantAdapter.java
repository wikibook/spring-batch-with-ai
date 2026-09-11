package kr.co.wikibook.logbatch.atom;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

public class InstantAdapter extends XmlAdapter<String, Instant> {
  @Override
  public Instant unmarshal(String string) {
    return Instant.parse(string);
  }

  @Override
  public String marshal(Instant instant) {
    return instant.toString();
  }
}
