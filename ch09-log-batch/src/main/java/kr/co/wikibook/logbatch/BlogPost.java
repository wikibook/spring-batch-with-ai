package kr.co.wikibook.logbatch;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import kr.co.wikibook.logbatch.atom.InstantAdapter;

@XmlRootElement(name = "post")
public class BlogPost {
  private String title;
  private String url;
  private Instant updatedAt;

  public BlogPost() { }

  public BlogPost(String title, String url, Instant updatedAt) {
    this.title = title;
    this.url = url;
    this.updatedAt = updatedAt;
  }

  @XmlJavaTypeAdapter(InstantAdapter.class)
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}