# Spring Batch 4 → 6 / Spring Boot 2.7 → 4 업그레이드 작업 지시서

**기준 버전: Spring Batch 6.0.6.** 공통 지침의 버전 기준과 의존성 확인 절차를 따른다.

이 문서는 `spring-batch-5-to-6.md`를 전제로 한다. **두 문서를 함께 컨텍스트에 넣는다.**
작업 원칙, 작업 전 확인 사항, 완료 보고 형식, 완료 후 검수 포인트, 공식 참고 자료는 5→6 문서를 그대로 따른다.
이 문서에는 다음 두 가지만 적는다.

- **4 전용 절**: Batch 4 / Boot 2.7에서 올 때만 필요한 변경. 절 번호를 A, B, C, ...로 붙였다.
- **보충**: 5→6 문서의 절을 적용할 때 Batch 4 프로젝트에서 추가로 볼 내용.

## 이 문서만의 원칙

- **최종 코드는 6.x API를 목표로 한다.** 5.x 시절의 중간 형태(예: `.chunk(int, TransactionManager)` 2-인자 시그니처, Batch 5까지의 `org.springframework.batch.item.*` 경로, `JobLauncherTestUtils`)는 6.x에서 deprecated 또는 이동됐다. 동등한 동작을 확인한 뒤 최종 형태로 교체한다. 필요하면 격리된 환경에서 중간 버전을 검증할 수 있으며, 최종 코드가 6.x라고 해서 4→5의 스키마·직렬화 변경을 생략하지 않는다. 5→6 문서의 표에서 '이전' 열은 Batch 5 형태이므로, Batch 4 형태에서 '변경 후' 열로 바로 간다.
- Boot 2.7 → 4는 메이저 두 단계를 건너뛴다. 배치 외 영역(웹, 시큐리티, 데이터 접근 등)은 아래 공식 가이드를 따로 적용한다. 이 문서는 배치 부분만 다룬다.
  - Spring Boot 3.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
  - Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

## 작업 전 확인 사항 (추가)

- E절과 5→6 문서 16절을 먼저 읽고, 이력 보존 요구·미완료 실행·공유 애플리케이션·직렬화 설정을 조사한다. 기존 저장소 이행과 새 저장소 분리 중 전환 전략을 정한다. 결정이 필요한 동안에도 의존하지 않는 코드 조사와 격리된 테스트 준비는 계속할 수 있다.

## 적용 순서

5→6 문서의 '적용 순서'를 따르되 아래처럼 관련 절을 묶는다. 절 번호 순서보다 컴파일·기동의 의존 관계를 우선한다.

| 작업 묶음 | 적용할 절 | 확인할 내용 |
|---|---|---|
| 사전 조사·전환 계획 | **E**, 5→6 **16절** | 메타DB 스키마·이력·ExecutionContext·미완료 실행 |
| 빌드 기준선 | **A**, 5→6 **0절** + 보충 | 정확한 목표 버전, JDK·빌드 도구, Jakarta, 스타터 |
| 패키지·컴포넌트 | **C**, 5→6 **1·3절** + 보충 | import, Writer의 Chunk, 초기화 호출 |
| 잡·스텝과 오류 처리 | **B**, 5→6 **2·7·8절** + 보충 | 빌더, 재시도·skip, 리스너 호출 시점 |
| 실행·저장소 구성 | **D**, 5→6 **4·5·6·9·10절** + 보충 | JobOperator, 저장소, 트랜잭션, 속성 |
| 모델·파라미터·직렬화 | **F**, 5→6 **12·13·15절** + 보충 | 생성자·시간 타입·Jackson·record·incrementer |
| 기타 사용 기능 | **G**, 5→6 **11·14·17절** | JSR-352·JAXB, 컨텍스트 격리, Framework API, 모니터링 |

사용 중인 기능의 의존 변경까지 처리한 뒤 전체 빌드·테스트를 실행한다. 이후 동일 입력 결과, 실패 후 재시작, 중복 실행, 리스너·트랜잭션 동작과 전환 리허설을 검증하고 공통 완료 보고 형식으로 보고한다.

## 4 전용 절

### A. 기준선 상승: JDK 17+, `javax` → `jakarta`

- 목표 버전의 JDK·빌드 도구 지원 범위를 확인한다. Boot 4.0의 최소 JDK는 17이며, 실제 실행·툴체인·배포 버전은 5→6 문서 0절에서 정한다.
- Jakarta EE로 이동한 API만 사용처와 의존성을 함께 바꾼다. `javax.*` 전체를 일괄 치환하지 않는다.

| 이전 (Batch 4 / Boot 2.7) | 변경 후 (Batch 6 / Boot 4) |
|---|---|
| `javax.annotation.PostConstruct`, `PreDestroy`, `Resource` 등 Jakarta로 이동한 애너테이션 | 대응하는 `jakarta.annotation.*` |
| `javax.inject.*` | `jakarta.inject.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.servlet.*` | `jakarta.servlet.*` |

`javax.sql.DataSource`, `javax.xml.parsers.*`, `javax.crypto.*` 등 JDK에 남아 있는 API는 그대로 둔다. `javax.annotation`의 nullability 애너테이션 등은 소속 라이브러리와 목표 대체 API를 따로 확인한다. JAXB는 G절을 따른다.

추천 순서: **JDK 먼저 17 이상으로 → 스프링 부트 버전 올리기(5→6 0절) → javax→jakarta 치환.**

검색:
```
rg "javax\.(annotation|inject|validation|persistence|servlet)\." src
```

### B. `JobBuilderFactory` / `StepBuilderFactory` 제거

Batch 4의 팩토리 방식은 사라졌다. `new JobBuilder(...)` / `new StepBuilder(...)`를 직접 호출하고, `JobRepository`를 생성자 인자로 넘긴다.

**잡**:
```java
// Batch 4
jobBuilderFactory.get("job").start(step).build();

// Batch 6
new JobBuilder("job", jobRepository).start(step).build();
```

**태스클릿 스텝**:
```java
// Batch 4
stepBuilderFactory.get("step").tasklet(t).build();

// Batch 6 — 트랜잭션이 필요한 경우
new StepBuilder("step", jobRepository).tasklet(t, transactionManager).build();

// Batch 6 — 트랜잭션이 필요 없는 태스클릿 (예: 파일 삭제 등)
new StepBuilder("step", jobRepository).tasklet(t).build();
```

**청크 지향 스텝**: Batch 5 세대의 `.chunk(int, TransactionManager)` 2-인자 오버로드는 Batch 6에서 deprecated다. 중간 형태를 거치지 말고 5→6 문서 2절의 최종 형태로 바로 간다.

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

Batch 4에서는 `@EnableBatchProcessing`이 등록한 트랜잭션 매니저를 암묵적으로 썼다. Batch 6에서는 트랜잭션 매니저를 생략할 수도 있으므로, 기존 트랜잭션 의미를 보존하도록 어떤 `PlatformTransactionManager`를 넘길지 명시적으로 결정한다. 업무 DB와 메타 DB가 다르면 청크 스텝에는 **업무 DB**의 트랜잭션 매니저를 넘긴다.

검색:
```
rg "JobBuilderFactory|StepBuilderFactory" src
```

### C. `ItemWriter.write(List)` → `write(Chunk)`

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

- `List`가 필요하면 `chunk.getItems()`를 호출한다.
- 테스트에서 `writer.write(List.of(...))`로 부르던 코드는 `writer.write(new Chunk<>(...))`로 바꾼다.
- `ItemWriteListener`의 `List` 시그니처도 함께 바뀌었다. 5→6 문서 8절 표를 따른다.

검색:
```
rg "void write\(List<" src
rg "\.write\(List\.of\(|\.write\(Arrays\.asList\(|\.write\(Collections\." src
```

### D. `@EnableBatchProcessing` 역할 변화와 `BatchConfigurer` 제거

Batch 4의 기반 구성을 조사한 뒤, Batch 6에서는 Boot 자동 설정을 사용할지 명시적 구성을 사용할지 선택한다. 기존 BatchConfigurer의 커스터마이징을 확인하지 않고 애너테이션만 제거하지 않는다.

- `BatchConfigurer` 인터페이스와 `SimpleBatchConfiguration` 클래스 **삭제** (deprecated 단계 없이).
- `MapJobRepositoryFactoryBean`, `MapJobExplorerFactoryBean` 삭제.
- `JobBuilderFactory`, `StepBuilderFactory` 빈도 사라졌으므로(B절) `@EnableBatchProcessing`을 붙일 이유가 대부분 없어진다.

대체 방법 (현재 요구에 맞춰 하나를 선택):
1. Spring Boot 자동 설정 그대로 활용 (대부분 이것으로 충분). `application.properties`의 `spring.batch.*` 속성으로 세부 동작 지정. **`@EnableBatchProcessing`을 제거한다.**
2. 자바 코드로 지정해야 하는 경우 `@EnableBatchProcessing` + `@EnableJdbcJobRepository`. Batch 4에서 BatchConfigurer 등으로 지정하던 저장소 설정을 옮긴다. Batch 5의 `@EnableBatchProcessing`에서 제공하던 저장소 속성은 6에서 `@EnableJdbcJobRepository`로 이동했다. 속성 목록과 주의점은 5→6 문서 9절.
3. `JdbcDefaultBatchConfiguration` 상속 → 훅 메서드 오버라이드.

**중요**: 기반 구성은 세 방법 중 하나로 일관되게 정한다. 1번에 `@EnableBatchProcessing`을 덧붙이면 저장소 자동 설정이 물러날 수 있다. 다만 Boot 4의 기동 시 잡 실행 자동 설정은 별개이므로 `spring.batch.*` 전체가 무시된다고 가정하지 않는다. 기반 구성·스키마 초기화·잡 자동 실행을 나누어 검증한다(5→6 문서 6절).

`BatchConfigurer`로 하던 일의 대응:

| Batch 4 `BatchConfigurer` | Batch 6 |
|---|---|
| `getJobRepository()` 오버라이드로 데이터소스 지정 | 자동 설정: 메타 DB 데이터소스 빈에 `@BatchDataSource`. 코드: `@EnableJdbcJobRepository(dataSourceRef = ...)` |
| `getTransactionManager()` | 자동 설정: `@BatchTransactionManager`. 명시적 구성: 저장소는 `@EnableJdbcJobRepository(transactionManagerRef = ...)`, 실행기는 `@EnableBatchProcessing(transactionManagerRef = ...)`, 스텝은 빌더에서 각각 지정(5→6 문서 9절) |
| `getJobLauncher()`에서 `TaskExecutor` 지정 | 자동 설정: `@BatchTaskExecutor`. 코드: `@EnableBatchProcessing(taskExecutorRef = ...)` |
| 테스트용 `MapJobRepositoryFactoryBean` | 이력·재시작 등 저장소 기능을 검증하면 H2 또는 운영 DB와 같은 종류의 격리된 JDBC 저장소 |

`ResourcelessJobRepository`는 기존 Map 저장소의 일반적인 대체재가 아니다. 메타데이터를 저장하지 않고 스레드 안전하지 않으며, 재시작·ExecutionContext 공유·파티셔닝을 필요로 하지 않는 일회성 실행에 한정한다. 기존 테스트의 검증 목적을 유지한다. [공식 API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/repository/support/ResourcelessJobRepository.html)

검색:
```
rg "BatchConfigurer|SimpleBatchConfiguration|MapJobRepositoryFactoryBean|MapJobExplorerFactoryBean" src
rg "@EnableBatchProcessing" src
```

### E. 메타DB 스키마·직렬화 변경과 전환 전략

4→6에서는 **4→5와 5→6의 변경을 모두 검토한다**. 아래 표는 주요 변경이며 DB별 DDL 전체를 대신하지 않는다.

| 대상 | 주요 변경 |
|---|---|
| `BATCH_STEP_EXECUTION` | `CREATE_TIME` 추가, `START_TIME`의 NOT NULL 제약 해제 |
| `BATCH_JOB_EXECUTION` | `JOB_CONFIGURATION_LOCATION` 제거. 5.0 마이그레이션 스크립트는 이 칼럼을 지우지 않으므로 기존 저장소를 이행할 때 남아 있어도 된다 |
| `BATCH_JOB_EXECUTION_PARAMS` | `KEY_NAME` → `PARAMETER_NAME`, `TYPE_CD` → `PARAMETER_TYPE` |
| 파라미터 타입·값 | 타입은 클래스명, 값은 `PARAMETER_VALUE`에 문자열로 저장. 타입 열 길이 및 값 열 길이(250→2500) 변경, `DATE_VAL`·`LONG_VAL`·`DOUBLE_VAL` 제거 |
| 6.0 시퀀스 | `BATCH_JOB_SEQ` → `BATCH_JOB_INSTANCE_SEQ` |

파라미터 변경은 단순 컬럼 이름 변경만으로 끝나지 않는다. 기존 타입 코드와 날짜·숫자 값을 새 표현으로 변환할 수 있는지 검증한다. 목표 버전의 `spring-batch-core`에 포함된 `org/springframework/batch/core/migration/5.0/`, `migration/6.0/`와 DB별 schema SQL을 기준으로 실제 스키마 차이를 확인한다. Oracle·SQL Server 등 DB별 시퀀스 변경도 확인한다.

Batch 5에서는 기본 ExecutionContext serializer가 Jackson 기반에서 `DefaultExecutionContextSerializer`의 Base64 기반 직렬화로 바뀌었다. 실제 프로젝트의 커스텀 설정에 따라 기존 포맷은 다를 수 있다. 과거 컨텍스트의 역직렬화, 저장된 객체의 직렬화 가능 여부와 클래스 이동을 검증한다. Batch 6의 파라미터 구조·직렬화 변경도 추가로 적용되므로, 4의 실패 실행을 6에서 그대로 재시작할 수 있다고 가정하지 않는다. 미완료 실행은 기존 버전에서의 완료 또는 업무상 확정한 별도 재처리 계획으로 다룬다.

전환 전략은 요구에 따라 선택한다.

| 전략 | 적용 조건과 확인 사항 |
|---|---|
| 기존 저장소 이행 | 이력 보존·조회 요구가 있을 때 검토. DB별 DDL·값 변환·직렬화 호환성을 복제 데이터로 검증하고 공유 애플리케이션의 동시 전환 여부를 결정 |
| 새 접두어/스키마로 분리 | 이력을 별도 보관하고 새 저장소로 시작할 수 있을 때 검토. 과거 완료 이력·체크포인트·구버전과의 중복 실행 방지가 자동으로 이어지지 않음을 반영 |

접두어 분리를 선택하면 `BATCH6_` 등 실제 이름에 맞는 테이블·시퀀스 DDL을 준비하고 활성 저장소 구성에 같은 값을 지정한다. 속성만 바꾸면 새 스키마가 생성되거나 기존 이력이 이동한다고 가정하지 않는다. 구·신 버전의 스케줄러 전환, 업무 데이터 중복·누락, 롤백 범위는 5→6 문서 16절을 따른다. 접두어를 되돌려도 이미 변경된 업무 데이터는 복원되지 않는다.

근거: [5.0 공식 가이드](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide), [6.0 공식 가이드](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide).

검색:
```
rg "table-prefix|tablePrefix|JOB_CONFIGURATION_LOCATION|TYPE_CD|KEY_NAME|STRING_VAL|DATE_VAL|LONG_VAL|DOUBLE_VAL|BATCH_JOB_SEQ|ExecutionContextSerializer" src
```
SQL·배포 설정·운영 스크립트는 5→6 문서 16절의 전체 프로젝트 검색도 적용한다.

### F. `JobParameters`의 타입 보존과 명령행 형식

Batch 4에서는 파라미터가 `String`/`Long`/`Double`/`Date`로 제한됐다. Batch 5부터 임의 타입을 지원하고, Batch 6에서는 이름을 포함한 record와 Set 구조로 바뀐다. 생성자·접근자·순회·incrementer 변경은 5→6 문서 15절을 함께 적용한다.

**명령행 형식**: 기존 문자열 파라미터의 `key=value`는 여전히 유효하다. 기본 변환기의 타입과 identifying 표기는 선택 사항이다.
```
# 기존 String 타입과 identifying=true를 유지
java -jar batch.jar inputFile=file:/data/input.txt

# 같은 의미를 명시적으로 표현
java -jar batch.jar inputFile=file:/data/input.txt,java.lang.String,true
```
Batch 4에서 `name(long)=...` 같은 타입 표기나 identifying 접두어를 사용했다면 새 변환기 표기로 옮기되 이름·타입·값·identifying을 보존한다. 사용자 정의 변환기와 운영 스크립트·CI 호출부도 확인한다. 파일 경로 String을 Resource로 바꾸는 것은 필수 업그레이드가 아니다.

**`JobParametersBuilder`**: 기존 `addString`, `addLong`, `addDouble`, `addDate` 사용처는 타입을 유지한다. `Date`→`LocalDate`/`LocalDateTime`은 시간대·시각 정보와 인스턴스 식별에 영향을 줄 수 있으므로 별도 변경으로 다룬다. 업무상 타입을 변경하기로 정했다면 명령행 파싱·저장·조회·재시작 시 복원을 검증한다.

**태스클릿 내부**: Map 접근 자체는 필수 제거 대상이 아니다. 타입별 getter 활용은 5→6 문서 15절의 선택적 개선으로 적용한다.

검색:
```
rg "addDate\(|getDate\(|JobParametersConverter|JobParameter" src
rg "getStepContext\(\)\.getJobParameters\(\)" src
```
운영 스크립트의 실제 인자도 함께 조사한다.

### G. JSR-352·JAXB 같은 4.x 세대 이슈 정리 (일회성)

- **JSR-352**: Spring Batch의 JSR-352 구현은 5에서 제거됐다. `javax.batch.*`를 `jakarta.batch.*`로 치환하는 것으로 해결되지 않는다. JSR 잡 정의 XML·배치 아티팩트·실행 진입점을 조사하고 Spring Batch API와 설정으로 재작성한다. 실행·재시작 동등성을 검증한다. [공식 5.0 가이드](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide#jsr-352-implementation-removal)
- **JAXB 명시 의존성**: JAXB는 JDK 11에서 제거됐으므로 JDK 17이라는 이유로 의존성을 삭제하지 않는다. XML Reader/Writer, `Jaxb2Marshaller`, 생성된 바인딩 클래스의 사용을 확인하고 필요한 Jakarta XML Binding API·런타임의 호환 버전을 유지한다. 사용처가 없고 다른 라이브러리도 필요로 하지 않을 때만 제거한다. [JDK 제거 항목](https://docs.oracle.com/en/java/javase/17/migrate/removed-tools-and-components.html)

검색:
```
rg "javax\.batch\.|jakarta\.batch\.|javax\.xml\.bind|jakarta\.xml\.bind|Jaxb2Marshaller|StaxEventItem|batch-jobs" src
rg "jaxb|xml.bind" -g 'build.gradle*' -g 'pom.xml' -g '*.toml' .
```

## 5→6 문서 절에 대한 보충

### 0절 보충: 빌드 스크립트

- Boot 2.7 프로젝트의 `spring-boot-starter-batch`에는 JDBC 메타 저장소가 포함돼 있었다. Boot 4에서는 `spring-boot-starter-batch-jdbc`로 **바꿔야** 같은 동작이 유지된다. 0절의 '조용한 실패 경고'가 4에서 올 때 특히 잘 일어난다.
- Gradle 7.x면 부트 4 플러그인이 요구하는 Gradle 버전으로 래퍼를 먼저 올린다.

### 1절 보충: 패키지 재배치

- Batch 4의 패키지 경로는 Batch 5와 같으므로 1절의 표를 그대로 적용한다.
- `org.springframework.batch.core.configuration.annotation.JobBuilderFactory` / `StepBuilderFactory` import는 B절에서 제거된다.

### 8절 보충: 리스너

- Batch 4의 `ItemWriteListener`는 `List`를 받았고, `ChunkListener`는 `ChunkContext`를 받았다. 8절 표의 '이전' 열과 같다. 새 청크 모델의 리스너 호출 시점·트랜잭션 경계·동시 실행 제약도 함께 검증한다.
- `JobExecutionListenerSupport`, `StepExecutionListenerSupport` 상속은 Batch 4에서 흔했다. 인터페이스의 default 메서드로 대체한다.

### 10절 보충: 부트 속성명

Boot 2.7에는 이미 반영된 속성 변경도 있으므로 10절 표는 실제 사용처가 있을 때만 적용한다. 추가로 다음 변경을 확인한다.

| 이전 (Boot 2.7) | 변경 후 (Boot 4) | 반영 버전 |
|---|---|---|
| `spring.redis.*` | `spring.data.redis.*` | Boot 3.0 |

`spring-boot-properties-migrator`는 두 메이저를 건너뛰는 경우 특히 유용하다.

검색:
```
rg "spring\.batch\.(initialize-schema|schema|table-prefix|isolation-level-for-create)\b" src
rg "spring\.batch\.job\.names\b" src
rg "spring\.redis\." src
```

### 12절 보충: 시간 타입

`JobExecution` / `StepExecution`의 시간 타입이 5.0에서 `Date` → `LocalDateTime`으로 바뀌었다.

| Batch 4 | Batch 6 |
|---|---|
| `JobExecution.getStartTime()` → `Date` | `LocalDateTime` |
| `StepExecution.getEndTime()` → `Date` | `LocalDateTime` |

표시만 하는 코드는 LocalDateTime을 DateTimeFormatter에 넘길 수 있다. Instant·epoch·시간대 변환이 필요한 코드는 `.toInstant()`를 단순 삭제하지 말고 기존 DB/JVM의 시간대 의미를 확인한 뒤 명시적인 ZoneId로 변환한다. 시작·종료 시간이 없는 상태도 테스트한다.

검색:
```
rg "getStartTime\(\)\.toInstant|getEndTime\(\)\.toInstant|getCreateTime\(\)\.toInstant" src
```
