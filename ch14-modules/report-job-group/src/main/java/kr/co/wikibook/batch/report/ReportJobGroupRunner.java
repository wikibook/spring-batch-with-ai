package kr.co.wikibook.batch.report;

import kr.co.wikibook.batch.support.BatchConfig;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Import({BatchConfig.class, ReportJobGroupContexts.class})
@PropertySource("classpath:/job-db.properties")
public class ReportJobGroupRunner {
}
