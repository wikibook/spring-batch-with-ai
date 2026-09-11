package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.SystemCommandException;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;

class SystemCommandTest {
  SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();
  StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
  StepContribution stepContribution = new StepContribution(stepExecution);
  ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

  @DisplayName("셸 스크립트를 실행해서 작업 디렉터리에 결과 파일을 남긴다")
  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC}) // <1>
  void execute(@TempDir Path tempPath) throws Exception {
    // given
    var resource = new ClassPathResource("command.sh");
    String shellPath = resource.getFile().getAbsolutePath(); // <2>
    systemCommandTasklet.setCommand(shellPath); // <3>
    systemCommandTasklet.setTimeout(10000); // <4>
    systemCommandTasklet.setEnvironmentParams(new String[]{"MESSAGE=Hello"}); // <5>
    systemCommandTasklet.setWorkingDirectory(tempPath.toString()); // <6>
    systemCommandTasklet.afterPropertiesSet(); // <7>

    // when
    RepeatStatus taskStatus = systemCommandTasklet.execute(stepContribution, chunkContext);

    // then
    assertThat(stepContribution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    assertThat(taskStatus).isEqualTo(RepeatStatus.FINISHED);
    String content = Files.readString(tempPath.resolve("output.txt"));
    assertThat(content).isEqualTo("Hello\n"); // <8>
  }

  @DisplayName("명령이 타임아웃 안에 끝나지 않으면 SystemCommandException이 발생한다")
  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC}) // <1>
  void executeTimeout() throws Exception {
    systemCommandTasklet.setCommand("sleep", "3"); // <2>
    systemCommandTasklet.setTimeout(10);
    systemCommandTasklet.afterPropertiesSet();

    assertThatExceptionOfType(SystemCommandException.class)
        .isThrownBy(() -> systemCommandTasklet.execute(stepContribution, chunkContext))
        .withMessageContaining("Execution of system command did not finish within the timeout");
  }

  @DisplayName("커맨드 러너를 바꾸면 명령의 출력을 콘솔에 남긴다")
  @Test
  void executeEcho() throws Exception {
    systemCommandTasklet.setCommand("echo", "hello");
    systemCommandTasklet.setTimeout(10000);
    systemCommandTasklet.setCommandRunner(new ConsoleOutputCommandRunner());
    systemCommandTasklet.afterPropertiesSet();
    systemCommandTasklet.execute(stepContribution, chunkContext);
  }
}
