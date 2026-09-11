package kr.co.wikibook.healthchecker.url;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

public class LogResourceMetaTask implements Callable<RepeatStatus> {
	private final Logger logger = LoggerFactory.getLogger(LogResourceMetaTask.class);
	private final Resource resource;
	public LogResourceMetaTask(Resource resource) {
		this.resource = resource;
	}

	@Override
	public RepeatStatus call() throws IOException {
		File file = this.resource.getFile();
		Instant lastModified = Instant.ofEpochMilli(file.lastModified());
		logger.info("{} 파일 마지막 수정: {}", file.getName(), lastModified);
		return RepeatStatus.FINISHED;
	}
}
