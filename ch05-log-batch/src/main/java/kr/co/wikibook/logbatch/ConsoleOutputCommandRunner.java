package kr.co.wikibook.logbatch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.step.tasklet.CommandRunner;

public class ConsoleOutputCommandRunner implements CommandRunner {
  @Override
  public Process exec(String[] command, String[] environmentParams, File directory)
      throws IOException {
    ProcessBuilder builder = new ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .directory(directory);

    if (environmentParams != null) {
      Map<String, String> environment = builder.environment();
      environment.putAll(toMap(environmentParams)); // <3>
    }

    return builder.start();
  }

  private static Map<String, String> toMap(String[] source) {
    Map<String, String> dest = new HashMap<>();
    for (String pair : source) {
      String[] keyValue = pair.split("=", 2); // <4>
      dest.put(keyValue[0], keyValue[1]);
    }
    return dest;
  }
}
