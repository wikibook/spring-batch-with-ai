package kr.co.wikibook.batch.webadmin;

import java.util.Map;
import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {
  private final JobService service;

  public JobController(JobService service) {
    this.service = service;
  }

  @PostMapping("/jobs/{jobName}/start")
  public long start(@PathVariable String jobName, @RequestParam Map<String, String> parameters) {
    var jobParameters = new Properties();
    jobParameters.putAll(parameters);
    return service.start(jobName, jobParameters);
  }
}
