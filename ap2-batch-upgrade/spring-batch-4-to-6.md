# Spring Batch 4 → 6 / Spring Boot 2.7 → 4 업그레이드 작업 지시서

## 작업 원칙

- 아래 체크리스트를 **위에서 아래로 순서대로** 적용한다.
- 각 소절에 실린 `rg` 패턴을 돌려서 걸리는 파일을 빠짐없이 확인하고 교체한다.
- deprecated API는 무조건 비-deprecated 대체 API로 교체한다. "이전 형태도 동작한다"는 이유로 남겨 두지 않는다.
- 확신이 없는 지점이 나오면 **멈추고 사람에게 질문한다**. 추측으로 진행하지 않는다.
- 소절마다 컴파일과 테스트를 돌려 회귀가 없는지 확인한다.
- 변경 근거를 커밋 메시지에 해당 소절 제목으로 남긴다.
- 이 문서는 **4.x에서 6.x로 직접 가는** 변경만 적는다. 5.x 시절의 중간 API(예: `.chunk(int, TxMgr)` 2-인자 시그니처, Batch 5까지의 `org.springframework.batch.item.*` 경로)는 6.x에서 deprecated 또는 이동됐으므로, 중간 단계를 거치지 말고 바로 최종 형태로 교체한다.

검색 명령은 ripgrep(`rg`)을 기준으로 적었다(https://github.com/BurntSushi/ripgrep). 설치돼 있지 않으면 `grep -rE`로 바꿔도 된다.

## 작업 전 확인 사항

- 깨끗한 Git 작업 트리 + 현재 버전에서의 빌드·테스트 통과 스냅샷
- `./gradlew dependencies` 출력 스냅샷
- 메타데이터 테이블을 여러 배치 애플리케이션이 공유한다면 "테이블 접두어 분리"(2절) 전략을 먼저 결정할 것

## 체크리스트

### 1. 기준선 상승: JDK 17+, `javax` → `jakarta`

- JDK 17 이상 필수 (Boot 4에서는 21 권장).
- `javax.*` → `jakarta.*` 치환.

| 이전 (Batch 4 / Boot 2.7) | 변경 후 (Batch 6 / Boot 4) |
|---|---|
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.inject.*` | `jakarta.inject.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.servlet.*` | `jakarta.servlet.*` |

추천 순서: **JDK 먼저 17로 → 스프링 부트 버전 올리기 → javax→jakarta 치환.**

검색:
```
rg "javax\.(annotation|inject|validation|persistence|servlet)\." src
```

### 2. 메타DB 스키마 변경과 접두어 분리 전략

`BATCH_*` 테이블 스키마가 5.0에서 크게 바뀌었고 6에서도 그대로 유지된다.

| 테이블 | 변경 | 칼럼 |
|---|---|---|
| BATCH_STEP_EXECUTION | 추가 | CREATE_TIME |
| BATCH_JOB_EXECUTION_PARAMS | 이름 변경 | TYPE_CD → PARAMETER_TYPE |
| BATCH_JOB_EXECUTION_PARAMS | 이름 변경 | KEY_NAME → PARAMETER_NAME |
| BATCH_JOB_EXECUTION_PARAMS | 이름 변경 | STRING_VAL → PARAMETER_VALUE |
| BATCH_JOB_EXECUTION_PARAMS | 제거 | DATE_VAL / LONG_VAL / DOUBLE_VAL |

권장 전략: **테이블 접두어 분리.** 4.3 버전은 `BATCH_JOB_INSTANCE`, 6.0 버전은 `BATCH6_JOB_INSTANCE` 등으로 분리해 롤백 난이도를 낮춘다. 접두어 설정은 5절(저장소 설정) 참고.

공식 마이그레이션 스크립트: `spring-batch-core` 모듈의 `org/springframework/batch/core/migration/` 디렉터리.

### 3. Spring Boot 4의 배치 스타터 선택 (JDBC vs 몽고DB)

Spring Boot 4에서 배치 메타 저장소가 스타터 단위로 분리됐다. **JDBC와 몽고DB는 상호배타적 — 한 프로젝트에 함께 쓰지 않는다.**

| 스타터 | 용도 |
|---|---|
| `spring-boot-starter-batch` | 배치 코어. 메타 저장소 구현체 미포함 |
| `spring-boot-starter-batch-jdbc` | JDBC 메타 저장소 (기존 `BATCH_*` 테이블) |
| `spring-boot-starter-batch-data-mongodb` | 몽고DB 메타 저장소 (Spring Boot 4.1 신규) |

Batch 4를 쓰던 프로젝트는 대부분 JDBC이므로 특별한 사유가 없다면 `spring-boot-starter-batch-jdbc`를 그대로 쓴다.

자동 구성 클래스 패키지도 이동했다.

| 이전 (Boot 2.7 / 3) | 변경 후 (Boot 4) |
|---|---|
| `org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration` | `org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration` |
| `org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner` | `org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner` |
| `org.springframework.boot.autoconfigure.batch.BatchDataSource` | `org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource` |
| `BatchProperties$Jdbc` 내부 클래스 | `org.springframework.boot.batch.jdbc.autoconfigure.BatchJdbcProperties` |

`JobLauncherApplicationRunner` 생성자가 `(JobLauncher, JobExplorer, JobRepository)` 3-인자에서 `(JobOperator)` 단일 인자로 단순화됐다.

몽고DB 저장소로 가는 경우 주의점:
- `spring.batch.data.mongodb.schema.initialize=true`로 컬렉션 초기화
- 트랜잭션을 위해 반드시 Replica set(단일 노드라도)으로 띄움
- 테스트는 `MongoDBContainer` + `@ServiceConnection`

검색:
```
rg "org\.springframework\.boot\.autoconfigure\.batch\." src
rg "new JobLauncherApplicationRunner\([^)]*," src
rg "BatchProperties\s*\.\s*class|properties\.getJdbc\(\)" src
```

### 4. `@EnableBatchProcessing` 역할 변화와 `BatchConfigurer` 제거

Batch 4에서는 `@EnableBatchProcessing`이 기반 구성 요소를 직접 등록했지만, Batch 6에서는 Spring Boot 자동 설정이 그 역할을 한다.

- `BatchConfigurer` 인터페이스와 `SimpleBatchConfiguration` 클래스 **삭제** (deprecated 단계 없이).
- `MapJobRepositoryFactoryBean`, `MapJobExplorerFactoryBean` 삭제.

대체 방법 (우선순위 순):
1. Spring Boot 자동 설정 그대로 활용 (대부분 이것으로 충분). `application.properties`의 `spring.batch.*` 속성으로 세부 동작 지정.
2. `@EnableBatchProcessing`의 속성(`dataSourceRef`, `transactionManagerRef`, ...).
3. `JdbcDefaultBatchConfiguration` 상속 → 훅 메서드 오버라이드.

**중요**: 세 방법은 함께 쓰지 않는다. 1번(자동 설정)을 쓰는데 `@EnableBatchProcessing`을 덧붙이면 자동 설정이 물러나서 `spring.batch.*` 속성이 읽히지 않는다. Spring Boot로 배치를 쓰는 기본 전제는 `@EnableBatchProcessing`을 **붙이지 않는다**는 것이다.

테스트용 인메모리 저장소는 `ResourcelessJobRepository`(Batch 5.2+)로 교체.

검색:
```
rg "BatchConfigurer|SimpleBatchConfiguration|MapJobRepositoryFactoryBean|MapJobExplorerFactoryBean" src
rg "@EnableBatchProcessing" src
```

### 5. `@EnableJdbcJobRepository` / `@EnableMongoJobRepository` 도입

Batch 6.0부터 저장소별 전용 애너테이션이 생겼다. 3절의 스타터 선택과 짝을 이룬다. 두 애너테이션을 한 프로젝트에 함께 붙이지 않는다.

**중요**: 이 애너테이션들도 4절의 `@EnableBatchProcessing`과 마찬가지로 **자동 설정을 쓰는 경우에는 붙이지 않는다**. `application.properties`에 `spring.batch.jdbc.*` 속성만 지정하면 자동 설정이 `JobRepository`를 등록한다. 애너테이션을 덧붙이면 자동 설정이 비활성화되어 속성이 일부 무시된다.

자동 설정을 쓰지 않고 자바 코드로 직접 지정해야 하는 경우에는 `@EnableBatchProcessing`과 **함께** 쓴다.

```java
@EnableBatchProcessing(taskExecutorRef = "batchTaskExecutor")
@EnableJdbcJobRepository(dataSourceRef = "batchDataSource", tablePrefix = "BATCH6_")
class MyJobConfiguration { ... }
```

`isolationLevelForCreate` 값 타입이 `String` → `Isolation` enum으로 바뀌었다 (Batch 4에서는 String이었다).

| Batch 4 (String) | Batch 6 (enum) |
|---|---|
| `isolationLevelForCreate = "ISOLATION_REPEATABLE_READ"` | `isolationLevelForCreate = Isolation.REPEATABLE_READ` |

JDBC 저장소를 자바 코드로 직접 설정하는 방식이라면 `DefaultBatchConfiguration` 대신 `JdbcDefaultBatchConfiguration`을 상속한다.

검색:
```
rg "isolationLevelForCreate\s*=\s*\"ISOLATION_" src
rg "extends DefaultBatchConfiguration\b" src
rg "@EnableJdbcJobRepository|@EnableMongoJobRepository" src
```

### 6. 배치 관련 스프링 부트 속성명 변화

자동 설정으로 쓰는 프로젝트(4·5절의 기본 경로)에서는 **속성명 변경이 실질적인 업그레이드 작업**이다.
Boot 2.7 → 4.x 사이에 배치 관련 속성이 여러 단계에 걸쳐 이름이 바뀌었다.

| 이전 (Batch 4 / Boot 2.7) | 변경 후 (Batch 6 / Boot 4) | 반영 버전 |
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

### 7. `JobBuilderFactory` / `StepBuilderFactory` 제거와 빌더 시그니처

Batch 4의 팩토리 방식은 사라졌다. `new JobBuilder(...)` / `new StepBuilder(...)`를 직접 호출하고, `JobRepository`를 생성자 인자로 넘긴다.

**태스클릿 스텝**:
```java
// Batch 4
stepBuilderFactory.get("step").tasklet(t).build();

// Batch 6 — 트랜잭션이 필요한 경우
new StepBuilder("step", jobRepository).tasklet(t, transactionManager).build();

// Batch 6 — 트랜잭션이 필요 없는 태스클릿 (예: 파일 삭제 등)
new StepBuilder("step", jobRepository).tasklet(t).build();
```

**청크 지향 스텝**: Batch 5 세대의 `.chunk(int, TransactionManager)` 2-인자 오버로드는 **Batch 6에서 deprecated**. `.chunk(int)` 뒤에 `.transactionManager(...)`를 체이닝한다. Batch 4에서 직접 가는 독자는 중간 형태를 거치지 말고 바로 이 형태로 간다.

```java
// Batch 4
stepBuilderFactory.get("step")
    .<I, O>chunk(10)
    .reader(r).writer(w).build();

// Batch 6 (최종 형태)
new StepBuilder("step", jobRepository)
    .<I, O>chunk(10)
    .transactionManager(transactionManager)
    .reader(r).writer(w)
    .build();
```

`.chunk(CompletionPolicy)` 오버로드도 Batch 6에서 deprecated. `SimpleCompletionPolicy`, `TimeoutTerminationPolicy` 등을 `StepBuilder`에 직접 꽂던 코드는 `.chunk(int)` 형태로 바꾸거나 별도 커스텀 로직으로 빼낸다.

검색:
```
rg "JobBuilderFactory|StepBuilderFactory" src
rg "\.chunk\([^)]+,\s*[^)]*[Tt]ransactionManager" src
rg "\.chunk\([^)]*CompletionPolicy" src
```

### 8. 패키지 재배치 (Batch 6에서 `infrastructure` 분리)

Batch 6.0에서 배치 모듈이 `core`와 `infrastructure`로 물리 분리되면서 대부분의 타입 FQCN이 바뀌었다. Batch 4에서는 이동 전 경로를 썼으므로 import를 모두 교체한다.

| 이전 (Batch 4) | 변경 후 (Batch 6) |
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

### 9. `JobLauncher` → `JobOperator` 통합

Batch 6에서 `JobLauncher` 인터페이스와 `TaskExecutorJobLauncher` 구현이 모두 deprecated. `JobOperator`로 통합됐다.

| Batch 4 | Batch 6 |
|---|---|
| `JobLauncher` 인터페이스 주입 | `JobOperator` 인터페이스 주입 |
| `new TaskExecutorJobLauncher()` | `new TaskExecutorJobOperator()` |
| `JobLauncher.run(Job, JobParameters)` | `JobOperator.start(Job, JobParameters)` |
| `JobLauncherTestUtils.launchJob(...)` | `JobOperatorTestUtils.startJob(...)` |
| `JobLauncherTestUtils.launchStep(String)` | `JobOperatorTestUtils.startStep(String)` |
| `StepBuilder.job(Job).launcher(...)` | `StepBuilder.job(Job).operator(...)` |

`JobOperator` 본체 메서드도 문자열·Long 기반에서 도메인 객체 기반으로 바뀌었다.

| Batch 4 | Batch 6 |
|---|---|
| `operator.start(String jobName, Properties)` | `operator.start(Job, JobParameters)` |
| `operator.stop(long executionId)` | `operator.stop(JobExecution)` |
| `operator.restart(long executionId)` | `operator.restart(JobExecution)` |
| `operator.startNextInstance(String jobName)` | `operator.startNextInstance(Job)` |
| `operator.getRunningExecutions(String)` → `Set<Long>` | `JobRepository.findRunningJobExecutions(String)` → `Set<JobExecution>` |
| `operator.getJobNames()` | `JobRegistry.getJobNames()` |

검색:
```
rg "JobLauncherTestUtils|launchStep\(|launchJob\(" src
rg "TaskExecutorJobLauncher|\.launcher\(" src
rg "\bJobLauncher\b" src
rg "operator\.(getJobNames|getRunningExecutions|stop\(\s*[0-9a-zA-Z_]+\s*\)|restart\(\s*[0-9a-zA-Z_]+\s*\)|startNextInstance\(\s*\"|start\(\s*\")" src
```

### 10. `JobRepository`가 `JobExplorer`를 흡수

Batch 4에서 `JobRepository`(쓰기)와 `JobExplorer`(읽기)를 각각 주입받던 코드는, Batch 6에서 `JobRepository` 하나로 통일한다. `JobRepository extends JobExplorer`.

### 11. `modular=true` / `ApplicationContextFactory` 패턴 폐기

Batch 4에서 잡별로 독립된 애플리케이션 컨텍스트를 띄우던 전통적인 패턴은 Batch 6에서 전면 deprecated.

| Batch 4 | Batch 6 |
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

### 12. `ItemWriter` 시그니처 `List` → `Chunk` + 리스너 시그니처

`ItemWriter.write()` 파라미터가 `List` → `Chunk`로 바뀌었다(5.0에서 최초 변경, 6까지 그대로).

```java
// Batch 4
public interface ItemWriter<T> {
    void write(List<? extends T> items) throws Exception;
}

// Batch 6
public interface ItemWriter<T> {
    void write(Chunk<? extends T> chunk) throws Exception;
}
```

`List`가 필요하면 `chunk.getItems()` 호출.

리스너 시그니처도 함께 바뀌었다.

| Batch 4 | Batch 6 |
|---|---|
| `ItemWriteListener.beforeWrite(List<? extends S>)` | `beforeWrite(Chunk<? extends S>)` |
| `ItemWriteListener.afterWrite(List<? extends S>)` | `afterWrite(Chunk<? extends S>)` |
| `ChunkListener.beforeChunk(ChunkContext)` | `beforeChunk(Chunk<I>)` (제네릭) |
| `ChunkListener.afterChunk(ChunkContext)` | `afterChunk(Chunk<O>)` |
| `ChunkListener.afterChunkError(ChunkContext)` | `onChunkError(Exception, Chunk<O>)` |
| `@AfterChunkError` | 애너테이션으로는 등록 불가. `ChunkListener`를 구현해 `onChunkError(Exception, Chunk<O>)` 재정의 |
| `ChunkListener.ROLLBACK_EXCEPTION_KEY` 상수 | deprecated (6.2 이후 제거) |
| `extends JobExecutionListenerSupport` | `implements JobExecutionListener` |
| `extends StepExecutionListenerSupport` | `implements StepExecutionListener` |

`ChunkListener`는 제네릭 타입 `ChunkListener<I, O>`로 바뀌었다. 여러 리스너를 한 클래스에 모으고 싶으면 `ItemListenerSupport`, `StepListenerSupport`는 그대로 남아 있으니 이쪽을 쓴다.

검색:
```
rg "void write\(List<" src
rg "beforeWrite\(List<|afterWrite\(List<|onWriteError\([^,]+,\s*List<" src
rg "afterChunkError|ROLLBACK_EXCEPTION_KEY|@AfterChunkError" src
rg "ChunkListener\b[^<]" src
rg "JobExecutionListenerSupport|StepExecutionListenerSupport" src
```

### 13. `JobParameters`의 타입 보존과 태스클릿 내 접근 방식

Batch 4에서는 파라미터가 `String`/`Long`/`Double`/`Date` 중 하나로 고정. Batch 5부터 임의 `Class<T>` 타입을 보존하도록 바뀌었고, 이 구조가 6까지 이어진다.

**명령행 형식**: 과거 `key=value` → 타입 힌트 포함 형식 권장.
```
# 과거 (Batch 4에서 잘 쓰던 형식)
java -jar batch.jar memoFile=file:/tmp/memo.txt

# 현재 (Batch 6 권장)
java -jar batch.jar memoFile=file:/tmp/memo.txt,org.springframework.core.io.Resource,true
```
세 번째 불리언은 identifying 플래그.

**태스클릿 내부**: `ChunkContext.getStepContext().getJobParameters()`(Map 반환)는 비권장. `StepContribution` 경유로 타입별 getter를 쓴다.
```java
// Batch 4 관용 (Map 기반)
Map<String, Object> params = ctx.getStepContext().getJobParameters();
LocalDate baseDate = (LocalDate) params.get("baseDate");

// Batch 6 권장
JobParameters params = contribution.getStepExecution().getJobParameters();
LocalDate baseDate = params.getLocalDate("baseDate");
Long chunkSize = params.getLong("chunkSize");
```

검색:
```
rg "chunkContext\.getStepContext\(\)\.getJobParameters\(\)" src
```

### 14. 도메인 객체의 시간 타입과 생성자

`JobExecution` / `StepExecution`의 시간 타입이 `Date` → `LocalDateTime`. 생성자 시그니처도 정리됐다.

| Batch 4 | Batch 6 |
|---|---|
| `new JobExecution(Long)` 단일 인자 | 제거. `new JobExecution(long, JobInstance, JobParameters)` |
| `new StepExecution(String, JobExecution, Long)` | `new StepExecution(long, String, JobExecution)` — 인자 순서 변경 |
| `JobExecution.getStartTime()` → `Date` | `LocalDateTime` |
| `StepExecution.getEndTime()` → `Date` | `LocalDateTime` |

테스트에서는 가능하면 생성자 대신 `MetaDataInstanceFactory`를 쓴다.

| 대신 | 이렇게 |
|---|---|
| `new JobExecution(0L, params)` | `MetaDataInstanceFactory.createJobExecution("testJob", 0L, 0L, params)` |
| `new JobExecution(0L)` | `MetaDataInstanceFactory.createJobExecution()` |
| `new StepExecution("name", jobExec)` | `MetaDataInstanceFactory.createStepExecution(jobExec, "name", 0L)` |

시간 비교/포매팅 코드에서 `.toInstant()` 호출이 남아 있으면 제거한다. `LocalDateTime`을 그대로 `DateTimeFormatter`에 넘기면 된다.

검색:
```
rg "new JobExecution\(|new StepExecution\(" src
rg "getStartTime\(\)\.toInstant|getEndTime\(\)\.toInstant" src
```

### 15. Jackson 2 → Jackson 3 (`tools.jackson`)

| Batch 4 | Batch 6 |
|---|---|
| `new JacksonJsonObjectReader<>(objectMapper, Clazz.class)` | `new JacksonJsonObjectReader<>(Clazz.class)` 또는 `(jsonMapper, Clazz.class)` |
| `new JacksonJsonObjectMarshaller<>(objectMapper)` | `new JacksonJsonObjectMarshaller<>()` 또는 `(jsonMapper)` |
| `com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.json.JsonMapper` |
| `registerModule(new JavaTimeModule())` | JSR-310 내장 지원 |
| `jackson-datatype-jsr310` 의존성 | 불필요 |
| `FAIL_ON_UNKNOWN_PROPERTIES` 기본 true | 기본 false |

검색:
```
rg "com\.fasterxml\.jackson" src
rg "JavaTimeModule|jackson-datatype-jsr310|Jackson2ObjectMapperBuilder" src
```

### 16. Spring Framework 7의 표준 `RetryPolicy`로 통합

Batch 4에서 `spring-retry` 모듈의 개별 정책/백오프 클래스를 조합하던 패턴은 Batch 6에서 단일 빌더 호출로 대체된다.

| Batch 4 (`spring-retry`) | Batch 6 (Spring Framework 7) |
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

### 17. JSpecify 애너테이션

Spring Framework 7에서 `org.springframework.lang.@Nullable` / `@NonNull` deprecated. JSpecify 표준으로 교체.

| Batch 4 / Spring 5 | Batch 6 / Spring 7 |
|---|---|
| `org.springframework.lang.Nullable` | `org.jspecify.annotations.Nullable` |
| `org.springframework.lang.NonNull` | `org.jspecify.annotations.NonNull` |

검색:
```
rg "org\.springframework\.lang\.(Nullable|NonNull)" src
```

### 18. JSR-352·JAXB 같은 4.x 세대 이슈 정리 (일회성)

- **JSR-352 `javax.batch.*` 인터페이스**: 제거. 4.x부터도 비권장이었고 6.x에서는 `jakarta.batch.*`로 이동했지만 실전에서 쓸 이유가 거의 없다.
- **JAXB 명시 의존성**: JDK 9 이후 표준에서 분리되어 Batch 4 시절 수동 의존성을 선언하던 시기가 있었다. JDK 17 기반의 Batch 6에서는 빌드 스크립트에서 제거해도 된다.

검색:
```
rg "javax\.batch\." src
rg "jaxb-(api|impl|runtime)" build.gradle pom.xml
```

## 완료 후 검수 포인트

에이전트가 작업을 마쳤다고 보고해도 사람이 한 번 더 본다.

- `./gradlew build -Xlint:deprecation` 출력에 deprecation 경고가 남아 있는지
- 운영 스크립트·CI 파이프라인의 잡 파라미터 형식이 타입 힌트 포함 형식인지
- 테이블 접두어 분리 마이그레이션을 쓴다면, 이 프로젝트가 실제로 읽는 **한 곳**(자동 설정의 `spring.batch.jdbc.table-prefix` / `@EnableJdbcJobRepository(tablePrefix=...)` / `JdbcDefaultBatchConfiguration.getTablePrefix()` 중 하나)에 새 접두어가 반영됐는지. 우선순위는 상속 > 애너테이션 > 자동 설정이라, 여러 곳에 중복해서 지정하면 의도한 값이 조용히 덮어써질 수 있다.
- JMX·관리자 웹 어드민처럼 `JobOperator`의 문자열 기반 메서드를 직접 쓰던 운영 도구의 호출부

## 공식 참고 자료

- Spring Batch 5.0 Migration Guide: https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide
- Spring Batch 6.0 Migration Guide: https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide
- Spring Boot 3.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
