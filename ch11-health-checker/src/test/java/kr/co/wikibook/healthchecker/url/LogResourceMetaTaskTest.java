package kr.co.wikibook.healthchecker.url;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.core.io.FileSystemResource;

class LogResourceMetaTaskTest {
	@Test
	void execute(@TempDir Path tempDir) throws IOException {
		// given
		Path resourcePath = tempDir.resolve(Path.of("url.txt"));
		Files.writeString(resourcePath, "");
		Instant lastModified = Instant.parse("2024-02-16T12:58:54.113Z");
		resourcePath.toFile().setLastModified(lastModified.toEpochMilli());
		var resource = new FileSystemResource(resourcePath);
		var task = new LogResourceMetaTask(resource);

		// when
		RepeatStatus status = task.call();

		// then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);
	}
}
