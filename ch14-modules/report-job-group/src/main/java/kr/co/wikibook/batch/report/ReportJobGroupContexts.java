package kr.co.wikibook.batch.report;

import kr.co.wikibook.batch.report.job.CreateAndSendReportJobConfig;
import kr.co.wikibook.batch.report.job.CreateReportJobConfig;
import kr.co.wikibook.batch.report.job.DeleteReportJobConfig;
import kr.co.wikibook.batch.report.job.SendReportJobConfig;
import kr.co.wikibook.batch.report.job.UserRankingJobConfig;
import org.springframework.context.annotation.Import;

@Import({
    UserRankingJobConfig.class,
    DeleteReportJobConfig.class,
    CreateReportJobConfig.class,
    SendReportJobConfig.class,
    CreateAndSendReportJobConfig.class
})
public class ReportJobGroupContexts {

}
