package kr.co.wikibook.logbatch;

import kr.co.wikibook.logbatch.atom.AtomEntry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.xml.StaxEventItemReader;
import org.springframework.batch.infrastructure.item.xml.StaxEventItemWriter;
import org.springframework.batch.infrastructure.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.infrastructure.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class CollectBlogPostJobConfig {

  public static final String JOB_NAME = "collectBlogPostJob";

  @Bean
  public Job collectBlogPostJob(JobRepository jobRepository) {

    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(new StepBuilder("collectBlogPostStep", jobRepository)
            .<AtomEntry, BlogPost>chunk(10)
            .reader(atomEntryXmlReader(null))
            .processor(new AtomEntryProcessor())
            .writer(blogPostXmlWriter(null))
            .build())
        .build();
  }

  @Bean
  public StaxEventItemReader<AtomEntry> atomEntryXmlReader(
      @Value("${blog.atom-url}") Resource resource) {
    var unmarshaller = new Jaxb2Marshaller();
    unmarshaller.setClassesToBeBound(AtomEntry.class);
    return new StaxEventItemReaderBuilder<AtomEntry>()
        .name("atomEntryXmlReader")
        .resource(resource)
        .unmarshaller(unmarshaller)
        .addFragmentRootElements("entry")
        .build();
  }

  @Bean
  public StaxEventItemWriter<BlogPost> blogPostXmlWriter(
      @Value("${blog.file}") WritableResource resource) {
    var marshaller = new Jaxb2Marshaller();
    marshaller.setClassesToBeBound(BlogPost.class);
    return new StaxEventItemWriterBuilder<BlogPost>()
        .name("blogPostWriter")
        .resource(resource)
        .marshaller(marshaller)
        .rootTagName("blog")
        .build();
  }
}
