# Spring Batch 5 → 6 / Spring Boot 3 → 4 업그레이드 작업 지시서

## 작업 원칙

- 아래 체크리스트의 각 소절을 **위에서 아래로 순서대로** 적용한다.
- 각 소절에 실린 `rg` 패턴을 돌려서 걸리는 파일을 빠짐없이 확인하고 교체한다.
- deprecated API는 무조건 비-deprecated 대체 API로 교체한다. "이전 형태도 동작한다"는 이유로 남겨 두지 않는다.
- 확신이 없는 지점이 나오면 **멈추고 사람에게 질문한다**. 추측으로 진행하지 않는다.
- 소절마다 컴파일과 테스트를 돌려 회귀가 없는지 확인한다.
- 변경 근거를 커밋 메시지에 해당 소절 제목으로 남긴다.

검색 명령은 ripgrep(`rg`)을 기준으로 적었다(https://github.com/BurntSushi/ripgrep). 설치돼 있지 않으면 `grep -rE`로 바꿔도 된다.

## 작업 전 확인 사항

- 깨끗한 Git 작업 트리 + 현재 버전에서의 빌드·테스트 통과 스냅샷
- `./gradlew dependencies` 출력 스냅샷

## 체크리스트

### 1. 패키지 재배치: `infrastructure` 서브모듈 분리

Spring Batch 6.0에서 배치 모듈이 `core`와 `infrastructure`로 물리적으로 분리됐다.

| 이전 (Batch 5까지) | 변경 후 (Batch 6) |
|---|---|
| `org.springframework.batch.core.Job` | `org.springframework.batch.core.job.Job` |
| `org.springframework.batch.core.Step` | `org.springframework.batch.core.step.Step` |
| `org.springframework.batch.core.JobExecution` | `org.springframework.batch.core.job.JobExecution` |
| `org.springframework.batch.core.StepExecution` | `org.springframework.batch.core.step.StepExecution` |
| `org.springframework.batch.core.StepContribution` | `org.springframework.batch.core.step.StepContribution` |
| `org.springframework.batch.core.JobParameters` | `org.springframework.batch.core.job.parameters.JobParameters` |
| `org.springframework.batch.core.JobParametersBuilder` | `org.springframework.batch.core.job.parameters.JobParametersBuilder` |
| `org.springframework.batch.item.ItemReader/ItemWriter/ItemProcessor` | `org.springframework.batch.infrastructure.item.*` |
| `org.springframework.batch.item.ExecutionContext` | `org.springframework.batch.infrastructure.item.ExecutionContext` |
| `org.springframework.batch.repeat.RepeatStatus` | `org.springframework.batch.infrastructure.repeat.RepeatStatus` |
| `org.springframework.batch.support.transaction.ResourcelessTransactionManager` | `org.springframework.batch.infrastructure.support.transaction.ResourcelessTransactionManager` |
| 리스너 인터페이스 (`JobExecutionListener` 등) | `org.springframework.batch.core.listener.*` |

검색:
```
rg "org\.springframework\.batch\.core\.(StepContribution|StepExecution|JobExecution|JobParameters|JobParametersBuilder|Job\b|Step\b)" src
rg "org\.springframework\.batch\.item\." src
rg "org\.springframework\.batch\.repeat\." src
rg "org\.springframework\.batch\.support\.transaction\." src
rg "org\.springframework\.batch\.core\.(JobExecutionListener|StepExecutionListener|ChunkListener|ItemReadListener|ItemProcessListener|ItemWriteListener|SkipListener)\b" src
```

### 2. JobLauncher / JobLauncherTestUtils → JobOperator / JobOperatorTestUtils

`JobLauncher` 인터페이스와 `TaskExecutorJobLauncher` 구현이 모두 `@Deprecated`. `JobOperator`로 통합됐다.

| 이전 | 변경 후 |
|---|---|
| `JobLauncher` 인터페이스 주입 | `JobOperator` 인터페이스 주입 |
| `new TaskExecutorJobLauncher()` | `new TaskExecutorJobOperator()` |
| `JobLauncher.run(Job, JobParameters)` | `JobOperator.start(Job, JobParameters)` |
| `JobLauncherTestUtils.launchJob(...)` | `JobOperatorTestUtils.startJob(...)` |
| `JobLauncherTestUtils.launchStep(String)` | `JobOperatorTestUtils.startStep(String)` |
| `StepBuilder.job(Job).launcher(...)` | `StepBuilder.job(Job).operator(...)` |

`JobOperator` 본체 메서드도 도메인 객체 기반으로 바뀌었다.

| 이전 | 변경 후 |
|---|---|
| `operator.start(String jobName, Properties)` | `operator.start(Job, JobParameters)` |
| `operator.stop(long executionId)` | `operator.stop(JobExecution)` |
| `operator.restart(long executionId)` | `operator.restart(JobExecution)` |
| `operator.startNextInstance(String jobName)` | `operator.startNextInstance(Job)` |
| `operator.getRunningExecutions(String jobName)` → `Set<Long>` | `JobRepository.findRunningJobExecutions(String)` → `Set<JobExecution>` |
| `operator.getJobNames()` | `JobRegistry.getJobNames()` |

검색:
```
rg "JobLauncherTestUtils|launchStep\(|launchJob\(" src
rg "TaskExecutorJobLauncher|\.launcher\(" src
rg "\bJobLauncher\b" src
rg "operator\.(getJobNames|getRunningExecutions|stop\(\s*[0-9a-zA-Z_]+\s*\)|restart\(\s*[0-9a-zA-Z_]+\s*\)|startNextInstance\(\s*\"|start\(\s*\")" src
```

### 3. `modular=true` / `ApplicationContextFactory` 패턴 폐기

| 제거/비권장 | 대체 |
|---|---|
| `@EnableBatchProcessing(modular = true)` | 단일 컨텍스트 + 잡 그룹별 `@Configuration` + `@Import` |
| `ApplicationContextFactory` / `GenericApplicationContextFactory` 빈 | 잡 그룹 설정 클래스에 `@Import({JobA.class, ...})` |
| `JobLocator` 인터페이스 | `JobRegistry` |

검색:
```
rg "modular\s*=\s*true" src
rg "ApplicationContextFactory|GenericApplicationContextFactory|AbstractApplicationContextFactory" src
rg "\bJobLocator\b" src
```

### 4. `JobRepository`가 `JobExplorer`를 흡수

Batch 6.0부터 `JobRepository extends JobExplorer`. 두 인터페이스를 각각 주입받던 코드는 `JobRepository` 하나로 통일.

### 5. Spring Boot 4의 배치 스타터·패키지 재배치

자동 구성 클래스 패키지 이동:

| 이전 (Boot 3) | 변경 후 (Boot 4) |
|---|---|
| `org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration` | `org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration` |
| `org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner` | `org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner` |
| `org.springframework.boot.autoconfigure.batch.BatchDataSource` | `org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource` |
| `BatchProperties$Jdbc` 내부 클래스 | `org.springframework.boot.batch.jdbc.autoconfigure.BatchJdbcProperties` |

배치 스타터가 저장소별로 분리됐다. **JDBC와 몽고DB는 상호배타적 — 한 프로젝트에 함께 쓰지 않는다.**

| 스타터 | 용도 |
|---|---|
| `spring-boot-starter-batch` | 배치 코어. 메타 저장소 구현체 미포함 |
| `spring-boot-starter-batch-jdbc` | JDBC 메타 저장소 |
| `spring-boot-starter-batch-data-mongodb` | 몽고DB 메타 저장소 (Spring Boot 4.1 신규) |

`JobLauncherApplicationRunner` 생성자가 `(JobLauncher, JobExplorer, JobRepository)` 3-인자에서 `(JobOperator)` 단일 인자로 단순화됐다.

검색:
```
rg "org\.springframework\.boot\.autoconfigure\.batch\." src
rg "new JobLauncherApplicationRunner\([^)]*," src
rg "BatchProperties\s*\.\s*class|properties\.getJdbc\(\)" src
```

### 6. `@EnableJdbcJobRepository` / `@EnableMongoJobRepository` 도입

저장소별 전용 애너테이션. 5절의 스타터 선택과 짝을 이룬다. 두 애너테이션은 함께 붙이지 않는다.

**중요**: `@EnableBatchProcessing`과 `@EnableJdbcJobRepository`(및 `@EnableMongoJobRepository`)는 **자동 설정을 쓰는 경우에는 붙이지 않는다**. `application.properties`에 `spring.batch.jdbc.*` 같은 속성만 지정하면 스프링 부트 자동 설정이 `JobRepository`를 등록한다. 애너테이션을 덧붙이면 자동 설정이 비활성화되어 속성이 일부 무시된다. 애너테이션은 저장소 세부 옵션을 자바 코드로 지정하려는 경우에만 쓴다.

애너테이션을 꼭 써야 한다면 `@EnableBatchProcessing`과 **함께** 쓴다 (대체가 아니다).

```java
@EnableBatchProcessing(taskExecutorRef = "batchTaskExecutor")
@EnableJdbcJobRepository(dataSourceRef = "batchDataSource")
class MyJobConfiguration { ... }
```

`isolationLevelForCreate` 값 타입이 `String` → `Isolation` enum.

| 이전 | 변경 후 |
|---|---|
| `isolationLevelForCreate = "ISOLATION_REPEATABLE_READ"` | `isolationLevelForCreate = Isolation.REPEATABLE_READ` |

`DefaultBatchConfiguration` 상속 방식이면 `JdbcDefaultBatchConfiguration`으로 슈퍼클래스 변경.

몽고DB 저장소 주의점:
- `spring.batch.data.mongodb.schema.initialize=true`로 컬렉션 초기화
- 트랜잭션을 위해 반드시 Replica set(단일 노드라도)으로 띄움
- 테스트는 `MongoDBContainer` + `@ServiceConnection`

검색:
```
rg "isolationLevelForCreate\s*=\s*\"ISOLATION_" src
rg "extends DefaultBatchConfiguration\b" src
rg "@EnableBatchProcessing|@EnableJdbcJobRepository|@EnableMongoJobRepository" src
```

### 7. 배치 관련 스프링 부트 속성명 변화

자동 설정으로 쓰는 프로젝트에서는 **속성명 변경이 실질적인 업그레이드 작업**이다.

| 이전 | 변경 후 | 반영 버전 |
|---|---|---|
| `spring.batch.initialize-schema` | `spring.batch.jdbc.initialize-schema` | Boot 2.5 |
| `spring.batch.schema` | `spring.batch.jdbc.schema` | Boot 2.5 |
| `spring.batch.table-prefix` | `spring.batch.jdbc.table-prefix` | Boot 2.5 |
| `spring.batch.isolation-level-for-create` | `spring.batch.jdbc.isolation-level-for-create` | Boot 2.5 |
| `spring.batch.job.names` (콤마 구분) | `spring.batch.job.name` (단일 값) | Boot 3.0 |
| `spring.redis.*` | `spring.data.redis.*` | Boot 3.0 |
| _(없음)_ | `spring.batch.data.mongodb.*` | Boot 4.1 신규 |

일괄 진단에는 `spring-boot-properties-migrator` 모듈이 유용하다. `build.gradle`에 런타임 의존성으로 추가하면 부팅 시 옛 속성명과 권장 대체를 로그로 출력한다.

```groovy
dependencies {
    runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'
}
```

검증이 끝나면 이 의존성은 제거한다. 배치 외 속성 변경은 아래 공식 가이드를 참고한다.

- Spring Boot 3.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

검색:
```
rg "spring\.batch\.(initialize-schema|schema|table-prefix|isolation-level-for-create)\b" src
rg "spring\.batch\.job\.name\b" src
rg "spring\.redis\." src
```

### 8. Jackson 2 → Jackson 3 (`tools.jackson`)

| 이전 | 변경 후 |
|---|---|
| `new JacksonJsonObjectReader<>(objectMapper, Clazz.class)` | `new JacksonJsonObjectReader<>(Clazz.class)` 또는 `(jsonMapper, Clazz.class)` |
| `new JacksonJsonObjectMarshaller<>(objectMapper)` | `new JacksonJsonObjectMarshaller<>()` 또는 `(jsonMapper)` |
| `com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.json.JsonMapper` |
| `registerModule(new JavaTimeModule())` | JSR-310 내장 |
| `jackson-datatype-jsr310` 의존성 | 불필요 |
| `FAIL_ON_UNKNOWN_PROPERTIES` 기본 true | 기본 false |

검색:
```
rg "com\.fasterxml\.jackson" src
rg "JavaTimeModule|jackson-datatype-jsr310|Jackson2ObjectMapperBuilder" src
```

### 9. Spring Framework 7의 표준 `RetryPolicy`로 통합

| 이전 (`spring-retry`, Batch 5까지) | 변경 후 (Spring Framework 7 / Batch 6) |
|---|---|
| `new SimpleRetryPolicy(maxAttempts, Map.of(Ex.class, true))` | `RetryPolicy.builder().maxRetries(n).includes(Ex.class).build()` |
| `new TimeoutRetryPolicy()` + `setTimeout(ms)` | `RetryPolicy.builder().delay(...)` |
| `new FixedBackOffPolicy()` + `setBackOffPeriod(ms)` | `RetryPolicy.builder().delay(Duration.ofMillis(ms))` |
| `new ExponentialBackOffPolicy()` + `setInitialInterval/Multiplier/MaxInterval` | `RetryPolicy.builder().delay(...).multiplier(...).maxDelay(...)` |
| `FaultTolerantStepBuilder.backOffPolicy(BackOffPolicy)` | `RetryPolicy.builder()`의 지연·배율로 대체 |

import: `org.springframework.retry.RetryPolicy` → `org.springframework.core.retry.RetryPolicy`

검색:
```
rg "org\.springframework\.retry\.(policy|backoff)\." src
rg "TimeoutRetryPolicy|SimpleRetryPolicy|FixedBackOffPolicy|ExponentialBackOffPolicy|CompositeRetryPolicy|BinaryExceptionClassifierRetryPolicy|MaxAttemptsRetryPolicy|CircuitBreakerRetryPolicy" src
rg "\.backOffPolicy\(" src
```

### 10. 리스너 인터페이스 시그니처 변화

| 이전 | 변경 후 |
|---|---|
| `ItemWriteListener.beforeWrite(List<? extends S>)` | `beforeWrite(Chunk<? extends S>)` |
| `ItemWriteListener.afterWrite(List<? extends S>)` | `afterWrite(Chunk<? extends S>)` |
| `ChunkListener.beforeChunk(ChunkContext)` | `beforeChunk(Chunk<I>)` (제네릭) |
| `ChunkListener.afterChunk(ChunkContext)` | `afterChunk(Chunk<O>)` |
| `ChunkListener.afterChunkError(ChunkContext)` | `onChunkError(Exception, Chunk<O>)` |
| `@AfterChunkError` | 애너테이션으로는 등록 불가. `ChunkListener`를 구현해 `onChunkError(Exception, Chunk<O>)` 재정의 |
| `ChunkListener.ROLLBACK_EXCEPTION_KEY` | deprecated (6.2 이후 제거) |
| `extends JobExecutionListenerSupport` | `implements JobExecutionListener` |
| `extends StepExecutionListenerSupport` | `implements StepExecutionListener` |

검색:
```
rg "beforeWrite\(List<|afterWrite\(List<|onWriteError\([^,]+,\s*List<" src
rg "afterChunkError|ROLLBACK_EXCEPTION_KEY|@AfterChunkError" src
rg "ChunkListener\b[^<]" src
rg "JobExecutionListenerSupport|StepExecutionListenerSupport" src
```

### 11. 도메인 객체의 시간 타입과 생성자 정리

| 이전 | 변경 후 |
|---|---|
| `new JobExecution(Long)` 단일 인자 | 제거. `new JobExecution(long, JobInstance, JobParameters)` |
| `new StepExecution(String, JobExecution, Long)` | `new StepExecution(long, String, JobExecution)` |
| `JobExecution.getStartTime()` → `Date` | `LocalDateTime` |
| `StepExecution.getEndTime()` → `Date` | `LocalDateTime` |

테스트에서는 가능하면 생성자 대신 `MetaDataInstanceFactory` 사용.

검색:
```
rg "new JobExecution\(|new StepExecution\(" src
rg "getStartTime\(\)\.toInstant|getEndTime\(\)\.toInstant" src
```

### 12. JSpecify 애너테이션

`org.springframework.lang.@Nullable` / `@NonNull`이 deprecated. JSpecify 표준(`org.jspecify.annotations.@Nullable` / `@NonNull`)으로 교체.

검색:
```
rg "org\.springframework\.lang\.(Nullable|NonNull)" src
```

### 13. `JobParameters` 타입별 getter 활용

`ChunkContext.getStepContext().getJobParameters()` (Map 반환) 대신 `StepContribution.getStepExecution().getJobParameters()` 사용 → 타입별 getter(`getLocalDate`, `getLong` 등) 직접 활용.

검색:
```
rg "chunkContext\.getStepContext\(\)\.getJobParameters\(\)" src
```

## 완료 후 검수 포인트

에이전트가 작업을 마쳤다고 보고해도 사람이 한 번 더 본다.

- `./gradlew build -Xlint:deprecation` 출력에 deprecation 경고가 남아 있는지
- 운영 스크립트·CI 파이프라인의 잡 파라미터 형식이 타입 힌트 포함 형식인지
- 테이블 접두어 분리 마이그레이션을 쓴다면, 이 프로젝트가 실제로 읽는 **한 곳**(자동 설정의 `spring.batch.jdbc.table-prefix` / `@EnableJdbcJobRepository(tablePrefix=...)` / `JdbcDefaultBatchConfiguration.getTablePrefix()` 중 하나)에 새 접두어가 반영됐는지. 우선순위는 상속 > 애너테이션 > 자동 설정이라, 여러 곳에 중복해서 지정하면 의도한 값이 조용히 덮어써질 수 있다.
- JMX·관리자 웹 어드민처럼 `JobOperator`의 문자열 기반 메서드를 직접 쓰던 운영 도구의 호출부

## 공식 참고 자료

- Spring Batch 6.0 Migration Guide: https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide
- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- Spring Boot 3.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
