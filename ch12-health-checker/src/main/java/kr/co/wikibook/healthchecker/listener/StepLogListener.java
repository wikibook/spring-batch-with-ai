package kr.co.wikibook.healthchecker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;

public class StepLogListener<T, S> extends StepListenerSupport<T, S> { // <1>

  private final Logger logger = LoggerFactory.getLogger(StepLogListener.class);

  @Override
  public void beforeStep(StepExecution stepExecution) {
    logger.info("beforeStep: {}", stepExecution);
  }

  @Override
  public void beforeChunk(Chunk chunk) {
    logger.info("beforeChunk: {}", chunk);
  }

  @Override
  public void beforeRead() {
    logger.info("beforeRead");
  }

  @Override
  public void afterRead(T item) {
    logger.info("afterRead. item={}", item);
  }

  @Override
  public void beforeProcess(T item) {
    logger.info("beforeProcess. item={}", item);
  }

  @Override
  public void afterProcess(T item, S result) {
    logger.info("afterProcess. {} -> {}", item, result);
  }

  @Override
  public void beforeWrite(Chunk<? extends S> chunk) {
    logger.info("beforeWrite. chunk={}", chunk);
  }

  @Override
  public void afterWrite(Chunk<? extends S> chunk) {
    logger.info("afterWrite. chunk={}", chunk);
  }

  @Override
  public void afterChunk(Chunk chunk) {
    logger.info("afterChunk");
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    logger.info("afterStep: {}", stepExecution);
    return ExitStatus.COMPLETED;
  }
}
