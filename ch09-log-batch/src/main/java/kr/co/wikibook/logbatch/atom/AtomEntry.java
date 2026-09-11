package kr.co.wikibook.logbatch.atom;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AtomEntry {
  private String title;
  private Instant updated;
  private Link link;
  private String content;

  public AtomEntry(String title, Instant updated,
                   Link link, String content) {
    this.title = title;
    this.updated = updated;
    this.link = link;
    this.content = content;
  }

  public AtomEntry() {
  }

  @XmlJavaTypeAdapter(InstantAdapter.class)
  public void setUpdated(Instant updated) {
    this.updated = updated;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Instant getUpdated() {
    return updated;
  }

  public Link getLink() {
    return link;
  }

  public void setLink(Link link) {
    this.link = link;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public static class Link {
    private String href;

    public Link() {
    }

    public Link(String href) {
      this.href = href;
    }

    @XmlAttribute
    public void setHref(String href) {
      this.href = href;
    }

    public String getHref() {
      return href;
    }
  }
}
