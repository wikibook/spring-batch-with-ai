package kr.co.wikibook.healthchecker.backup;

import java.time.Clock;
import java.util.concurrent.Callable;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnProperty(name = "spring.batch.job.name", havingValue = "backupDailyJob")
@Configuration
@Import(BackupRoute.class)
public class BackupDailyJobConfig {

  private final JobRepository jobRepository;

  public BackupDailyJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job backupDailyJob() {
    BackupFlowDecider decider = new BackupFlowDecider();

    return new JobBuilder("backupDailyJob", jobRepository)
        .start(checkDiskSpaceStep(null))
        .next(decider)
        .on("COMPLETED")
        .to(backupDailyStep(null))

        .from(decider)
        .on("RETRY")
        .to(deleteOldDirectoriesStep(null))
        .next(checkDiskSpaceStep(null))

        .from(decider)
        .on("FAILED")
        .fail()
        .end()
        .build();
  }

  @Bean
  @JobScope
  public Step checkDiskSpaceStep(BackupRoute route) {
    var tasklet = new CheckDiskSpaceTasklet(route);
    return new StepBuilder("checkDiskSpaceStep", jobRepository)
        .tasklet(tasklet)
        .build();
  }

  @Bean
  @JobScope
  public Step deleteOldDirectoriesStep(BackupRoute route) {
    var task = new DeleteOldDirectoriesTask(
        route.getTargetParentDirectory(), 10, Clock.systemDefaultZone()
    );
    return buildStep("deleteOldDirectoriesStep", task);
  }

  @Bean
  @JobScope
  public Step backupDailyStep(BackupRoute route) {
    var task = new BackupDailyTask(route, Clock.systemDefaultZone()
    );
    return buildStep("backupDailyStep", task);
  }

  private Step buildStep(String stepName, Callable<RepeatStatus> task) {
    var tasklet = new CallableTaskletAdapter(task);
    return new StepBuilder(stepName, jobRepository)
        .tasklet(tasklet)
        .build();
  }
}
