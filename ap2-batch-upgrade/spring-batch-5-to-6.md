# Spring Batch 5 → 6 / Spring Boot 3 → 4 업그레이드 작업 지시서

**기준 버전: Spring Batch 6.0.6.** 이 문서의 변경 후 API와 실행 동작은 6.0.6을 대상으로 한다. 더 높은 버전으로 올릴 때는 6.0.6 이후의 변경 사항을 추가로 확인한다. Spring Boot 4.x와 Spring Framework 7.x의 정확한 버전은 이 기준과 호환되는 조합으로 정한다.

## 작업 원칙

- **완료 조건은 기존 업무 동작과 운영 계약의 보존이다.** 잡·스텝 이름, 파라미터의 이름·타입·값·identifying 의미, 트랜잭션 경계, 재시작 동작을 임의로 변경하지 않는다.
- 아래 절을 먼저 훑어 해당 프로젝트의 적용 항목과 의존 관계를 정한다. 절 번호는 참조용이며, 실제 작업 순서는 아래 '적용 순서'를 따른다.
- 검색 결과는 **변경 후보**다. 실제 타입과 호출 맥락을 확인한 뒤 변경·유지·해당 없음으로 분류하고 이유를 기록한다. 검색 결과 0건만으로 완료를 판단하지 않는다.
- deprecated API는 목표 버전의 대체 API로 옮긴다. 동등한 대체가 확인되지 않으면 기존 기능을 삭제하거나 단순화하지 말고, 영향과 미완료 항목을 보고한다. 경고 억제나 테스트 삭제로 완료 조건을 맞추지 않는다.
- 필수 호환성 변경과 선택적 리팩터링을 구분한다. 파라미터 타입 변경, 컨텍스트 통합 등은 업그레이드에 꼭 필요한지 먼저 판단한다.
- 불확실한 API는 목표 버전의 공식 문서·소스·작은 검증 코드로 먼저 확인한다. 업무 정책이나 운영 전환 결정이 필요한 경우에는 근거와 선택지를 정리해 질문하고, 그 결정에 의존하지 않는 조사·수정·검증은 계속한다.
- 기존 사용자 변경을 보존한다. 작업 트리를 깨끗하게 만들기 위해 임의로 reset·삭제·stash하지 않는다. 변경 근거는 소절별로 기록하고, 커밋하는 경우 커밋 메시지에도 남긴다.
- 작업을 마치면 아래 '완료 보고 형식'대로 보고한다. 미검증 항목을 완료로 보고하지 않는다.

검색 명령은 ripgrep(`rg`)을 기준으로 적었다(https://github.com/BurntSushi/ripgrep). 설치돼 있지 않으면 사용 가능한 도구로 같은 범위를 검색한다. 정규식 문법 차이는 확인한다.

각 절의 `src` 검색은 단일 모듈 예시다. 먼저 모든 모듈과 소스 루트를 파악하고 반복 적용한다. import 외에 XML의 클래스명, 리플렉션 문자열, 테스트 코드도 확인한다. 속성과 의존성 검색에는 YAML, 실행 스크립트, CI, 컨테이너·배포 설정, 버전 카탈로그와 공통 빌드 플러그인도 포함한다. YAML 중첩 키는 점으로 연결된 속성명 검색만으로 찾을 수 없다.

```bash
# 대상 프로젝트 루트에서 실행. 문서와 생성물의 적중은 별도로 분류한다.
rg --files --hidden -g '!.git' -g '!**/build/**' -g '!**/target/**' -g '!**/.gradle/**'
rg --hidden -n -g '!.git' -g '!**/build/**' -g '!**/target/**' -g '!**/.gradle/**' 'spring-batch|spring-boot|spring\.batch|batch:|table-prefix|tablePrefix' .
```

## 작업 전 확인 사항

- Git 상태와 현재 버전에서의 빌드·테스트 결과를 기록한다. 기존 실패가 있으면 업그레이드로 발생한 실패와 구분한다.
- 현재와 목표의 **정확한** Boot·Batch·Spring Framework·JDK·Gradle/Maven 버전을 기록한다. Gradle 실행 JDK, 컴파일·테스트 툴체인, 배포 런타임 JDK를 구분하고 각 버전의 공식 지원 범위를 확인한다.
- 빌드 도구와 모든 모듈을 파악한다. Gradle이면 `./gradlew dependencies`와 필요한 모듈의 `dependencyInsight`, Maven이면 `./mvnw dependency:tree` 등으로 실제 해석된 의존성을 기록한다. 선언 버전만 확인하지 않는다.
- 잡·스텝, 실행 진입점, 프로파일, 스케줄러·CI 호출부, 파라미터와 incrementer를 조사한다. 동일 입력에 대한 업무 출력과 read/write/filter/skip 건수를 기준 결과로 확보한다.
- 메타 저장소 종류·DB 버전·접두어·공유 애플리케이션, 업무 DB와 메타 DB의 데이터소스·트랜잭션 매니저를 구분한다. 자동 설정·애너테이션·상속 중 현재 어떤 방식이 활성화되는지 확인한다.
- 기존 실행 이력 보존·조회 요구, 미완료 실행, ExecutionContext 직렬화 설정을 조사하고 **16절의 전환 계획을 실행 전에 정한다**. 4에서 올 때는 4→6 문서 E절도 적용한다.
- 재시도·skip·커스텀 CompletionPolicy·리스너·병렬 처리·파티셔닝·XML/모듈형 설정, 메트릭·추적·운영 알림의 사용 여부를 기록한다. 단순한 읽기·쓰기 잡으로는 검증되지 않는 기능이므로 프로젝트 자체 검증이 필요하다.

## 적용 순서

1. **사전 조사와 검증 계획**: 위 확인 사항과 16절을 검토한다. 테스트용 DB와 입력을 준비하고, 기존 결과 및 보존할 동작을 기록한다.
2. **컴파일·기동 복구**: 0~6절을 중심으로 적용하되, 사용 중인 기능에 따라 7~15절의 관련 변경도 같은 묶음으로 처리한다. 청크 빌더·재시도·리스너(2·7·8절), 저장소 구성·속성(0·6·9·10절), 도메인 생성자·Jackson·파라미터(12·13·15절)는 서로 연관될 수 있다. 의존 변경이 남아 있는 동안의 컴파일 실패는 진단에 활용한다.
3. **동작 검증**: 컴파일 복구 후 전체 빌드·테스트를 실행하고, 각 변경 기능의 성공·실패·재시작 경로와 17절의 모니터링을 검증한다. 같은 검사를 절마다 무조건 반복하지 말고 관련 변경 묶음과 실패 원인에 맞춰 실행한다.
4. **전환 리허설과 완료 보고**: 메타DB를 사용하는 경우 운영 DB와 같은 종류의 격리된 DB에서 스키마·이력 처리를 검증한다. 실제 실행 명령도 검증하며, 저장소를 사용하지 않는 경우 DB 항목은 해당 없음으로 기록한다. 배포·운영 DB 변경은 이 문서를 읽었다는 이유만으로 실행하지 않고, 해당 작업에 부여된 권한과 확정된 전환 계획을 따른다.

## 완료 보고 형식

1. 실제 사용한 버전, 대상 모듈, 활성 프로파일, 저장소·트랜잭션 구성.
2. 적용한 소절과 변경 요약. 검색 후보의 변경·유지·해당 없음 판정 및 이유. 건너뛴 절과 사유.
3. 실행한 빌드·테스트 명령, 결과와 로그/보고서 위치. deprecation 검사 결과와 남은 경고·억제 위치 및 이유. 테스트 생략 여부와 사유.
4. 동일 입력의 업무 출력·처리 건수 비교, 메타데이터 저장, 실패 후 재시작, 중복 실행, 재시도·skip·롤백·리스너 검증 결과. 해당 기능이 없으면 해당 없음으로 표시한다.
5. 스키마·이력 전환 계획과 리허설 결과, 기동 시 자동 실행 여부와 모니터링 검증 결과, 운영 반영 여부, 미검증 항목·남은 결정·제약.

## 변경 항목

### 0. 빌드 스크립트: 버전 상향, 스타터 선택, deprecation 강제

아래 Gradle 스크립트는 구성 형태의 참조 예시다. 버전 값은 자리표시자이므로 확정한 목표 버전으로 바꾸고, JDK 버전도 대상 프로젝트의 지원 범위에 맞춘다. 목표 패치 버전과 검증 기준 문서의 버전·확인일을 먼저 기록한다. Batch 6.0.6을 기준으로 실제 해석된 의존성을 확인한다. 6.0.5 이하에서 발생한 문제를 재현했다는 이유만으로 6.0.6에 불필요한 우회 코드를 추가하지 않는다.

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.x.y'          // 확정한 Boot 4 패치 버전
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)        // 목표 Boot·Batch가 지원하는 JDK
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-batch-jdbc'   // JDBC 메타 저장소
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.batch:spring-batch-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += ['-Xlint:deprecation', '-Werror']
}
```

- 스프링 부트 플러그인/BOM을 확정한 4.x 패치 버전으로 올린다. Batch 6.0.6을 관리하는 호환 Boot BOM을 우선 사용한다. 선택한 BOM의 Batch 버전이 다르면 호환성을 확인해 Boot 버전을 조정하거나 Batch 버전을 명시적으로 override한다. core·infrastructure·test·integration 등 사용 중인 Batch 모듈의 실제 버전이 일치하는지 확인한다. 상위 Batch 버전을 선택했다면 6.0.6과의 차이도 기록한다.
- 목표 Boot·Batch 버전이 요구하는 JDK와 빌드 도구의 지원 범위에 맞춘다. Boot 4.0의 최소 JDK는 17이지만, 툴체인 설정만으로 Gradle 실행 JDK와 배포 런타임까지 바뀌지는 않는다.
- 테스트 의존성 `org.springframework.batch:spring-batch-test`는 그대로 둔다. `JobOperatorTestUtils`가 여기에 있다.
- Java 소스의 deprecation 검사를 켠다. 위 예시의 `compilerArgs` 설정이 그것이며, Maven은 compiler plugin의 해당 옵션을 사용한다. Kotlin 등 다른 언어는 해당 컴파일러의 경고 검사도 설정한다. `-Werror`는 발생한 다른 경고도 오류로 처리하므로 원인을 구분한다. XML·리플렉션·실행 동작은 이 검사로 검증되지 않는다.

**스타터 선택.** 스프링 부트 4에서 배치 메타 저장소가 스타터 단위로 분리됐다.

| 스타터 | 용도 |
|---|---|
| `spring-boot-starter-batch` | 배치 코어. 메타 저장소 구현체 미포함 |
| `spring-boot-starter-batch-jdbc` | JDBC 메타 저장소 (기존 `BATCH_*` 테이블). `spring-boot-starter-batch`를 포함한다 |
| `spring-boot-starter-batch-data-mongodb` | 몽고DB 메타 저장소 (Spring Boot 4.1 신규) |

JDBC 메타 저장소를 쓰던 프로젝트는 `spring-boot-starter-batch`를 `spring-boot-starter-batch-jdbc`로 **바꾼다**. 두 줄을 같이 두지 않아도 된다. 자동 설정으로 사용할 메타 저장소는 기존 운영 요구에 맞춰 하나를 선택한다. 애플리케이션의 업무용 JDBC/MongoDB 사용 여부와 메타 저장소 선택을 혼동하지 않는다. MongoDB 스타터는 목표 Boot 버전에서 제공되는지 확인한다.

**조용한 실패 경고.** `spring-boot-starter-batch`만 남겨도 컴파일과 기동은 된다. 이때 부트 자동 설정은 `DefaultBatchConfiguration`을 등록하고, 이 클래스의 `JobRepository`는 메타데이터를 저장하지 않는 `ResourcelessJobRepository`다. 잡은 돌지만 메타데이터가 어디에도 저장되지 않아 재시작과 중복 실행 방지가 사라진다. 업그레이드 뒤 실제 저장소 빈과 설정을 확인하고, JDBC라면 실제 접두어의 테이블에 새 실행의 메타데이터가 저장되는지 검증한다. 저장소를 유지해야 하는 테스트를 resourceless 구성으로 바꿔 통과시키지 않는다.

검색:
```
rg "spring-boot-starter-batch" -g 'build.gradle*' -g 'pom.xml' -g '*.toml' .
rg "Xlint:deprecation" -g 'build.gradle*' .
```

### 1. 패키지 재배치: `infrastructure` 서브모듈 분리

Spring Batch 6.0에서 배치 모듈이 `core`와 `infrastructure`로 물리적으로 분리됐고, `core` 루트에 있던 도메인 클래스 대부분이 하위 패키지로 내려갔다.

**infrastructure로 이동**

| 이전 (Batch 5까지) | 변경 후 (Batch 6) |
|---|---|
| `org.springframework.batch.item.*` | `org.springframework.batch.infrastructure.item.*` |
| `org.springframework.batch.repeat.*` | `org.springframework.batch.infrastructure.repeat.*` |
| `org.springframework.batch.support.*` | `org.springframework.batch.infrastructure.support.*` |
| `org.springframework.batch.poller.*` | `org.springframework.batch.infrastructure.poller.*` |

예: `ItemReader`, `ItemProcessor`, `ItemWriter`, `Chunk`, `ExecutionContext`, `RepeatStatus`, `ResourcelessTransactionManager`, `FlatFileItemReader`, `JdbcBatchItemWriter`가 모두 해당한다.

**core 루트에서 하위 패키지로 이동**

| 이전 (`org.springframework.batch.core.`) | 변경 후 (`org.springframework.batch.core.`) |
|---|---|
| `Job` | `job.Job` |
| `JobExecution` | `job.JobExecution` |
| `JobInstance` | `job.JobInstance` |
| `JobExecutionException`, `JobInterruptedException`, `UnexpectedJobExecutionException`, `StartLimitExceededException` | `job.*` |
| `JobParameters`, `JobParametersBuilder`, `JobParameter` | `job.parameters.*` |
| `JobParametersIncrementer`, `JobParametersValidator` | `job.parameters.*` |
| `JobParametersInvalidException` | **이름 변경**: `job.parameters.InvalidJobParametersException` |
| `Step` | `step.Step` |
| `StepExecution` | `step.StepExecution` |
| `StepContribution` | `step.StepContribution` |
| `JobExecutionListener`, `StepExecutionListener`, `ChunkListener`, `ItemReadListener`, `ItemProcessListener`, `ItemWriteListener`, `SkipListener`, `StepListener` | `listener.*` |
| `repository.JobExecutionAlreadyRunningException`, `repository.JobInstanceAlreadyCompleteException`, `repository.JobRestartException` | `launch.*` |
| `explore.JobExplorer` | `repository.explore.JobExplorer` |

**그대로 남는 것**: `ExitStatus`, `BatchStatus`, `Entity`는 `org.springframework.batch.core` 루트에 그대로 있다. 이 셋은 옮기지 않는다.

**함정**: `org.springframework.batch.core.repository.persistence` 아래에 `JobExecution`, `StepExecution`, `JobInstance`, `JobParameter`, `ExitStatus`라는 같은 이름의 영속화용 DTO가 있다. IDE 자동 import가 이쪽을 고르면 컴파일은 되면서 타입이 어긋난다. import는 반드시 위 표의 경로로 잡는다.

검색:
```
rg "^import org\.springframework\.batch\.(item|repeat|support|poller)\." src
rg "^import org\.springframework\.batch\.core\.[A-Z]" src | rg -v "core\.(ExitStatus|BatchStatus|Entity);"
rg "^import org\.springframework\.batch\.core\.(explore|repository\.(JobExecutionAlreadyRunning|JobInstanceAlreadyComplete|JobRestart))" src
rg "JobParametersInvalidException" src
rg "core\.repository\.persistence\." src
```

### 2. 청크 스텝 빌더: `.chunk(int, tm)` → `.chunk(int).transactionManager(tm)`

Batch 5의 `.chunk(int, PlatformTransactionManager)`와 `.chunk(CompletionPolicy, PlatformTransactionManager)` 오버로드가 Batch 6에서 deprecated다. `.chunk(int)` 뒤에 `.transactionManager(...)`를 체이닝한다.

```java
// Batch 5
new StepBuilder("step", jobRepository)
    .<I, O>chunk(10, transactionManager)
    .reader(r).writer(w)
    .build();

// Batch 6
new StepBuilder("step", jobRepository)
    .<I, O>chunk(10)
    .transactionManager(transactionManager)
    .reader(r).writer(w)
    .build();
```

이 교체는 빌더 클래스가 달라지는 변경이다.

- `.chunk(int, tm)`은 예전처럼 `SimpleStepBuilder`를 돌려주고 `TaskletStep`을 만든다.
- `.chunk(int)`는 Batch 6에 새로 생긴 `ChunkOrientedStepBuilder`를 돌려주고 `ChunkOrientedStep`을 만든다.
- 새 빌더에서는 `faultTolerant()`가 별도 `FaultTolerantStepBuilder`가 아니라 같은 빌더를 돌려준다. `retryLimit`과 `skipLimit`은 `int`가 아닌 `long`을 받는다. `retryLimit`은 횟수의 의미와 기본 대기 시간도 달라지므로 7절을 함께 적용한다.
- 새 빌더의 재시도 설정은 spring-retry가 아닌 Spring Framework 7의 `RetryPolicy`를 받는다. 재시도를 쓰는 스텝은 7절을 같이 적용한다.
- 옛 빌더 경로(`AbstractTaskletStepBuilder`)의 `listener(Object)`가 deprecated다. 애너테이션 기반 리스너 객체를 `.listener(obj)`로 등록하던 코드는 새 빌더로 옮기면 그대로 쓸 수 있다.

`.chunk(CompletionPolicy)` 사용처는 정책 구현과 설정값부터 확인한다. 고정 개수와 동등함을 입증한 경우에만 `.chunk(int)`로 옮긴다. 시간 제한·복합 조건·사용자 정의 종료 조건을 임의의 고정 크기로 치환하지 않는다. 동등한 구현이 필요하면 종료 조건과 커밋 경계를 보존하는 설계를 검증하고, 해결되지 않으면 해당 항목을 미완료로 보고한다.

청크 모델 교체 전후의 읽기·처리·쓰기, 재시도·skip, 롤백, ItemStream의 상태 저장과 재시작을 확인한다. 병렬 처리를 사용하면 처리 순서·스레드 안전성·리스너 호출 여부도 검증한다. 기존 트랜잭션 매니저를 생략하여 컴파일만 통과시키지 않는다.

**병렬 처리**: 새 청크 빌더의 `.taskExecutor(...)`는 `AsyncTaskExecutor`를 받는다. 이 경로에서는 Processor가 작업 스레드에서 병렬 실행되고 Reader·Writer는 스텝을 실행하는 메인 스레드에서 순차 실행된다. 기존의 청크 단위 병렬 실행과 같은 동작이라고 가정하지 않는다.

Spring 트랜잭션은 스레드에 묶이므로 작업 스레드의 Processor는 메인 스레드의 청크 트랜잭션에 참여하지 않는다. Processor가 DB를 갱신하거나 `MANDATORY` 전파를 요구하면 기존 동작이 달라질 수 있다. 별도 트랜잭션을 추가해도 Writer 실패 시 함께 롤백되는 원자성이 자동으로 보존되지 않는다. Processor의 DB 변경 후 Writer를 실패시키는 테스트로 결과를 확인하고, 원자성이 필요하면 처리 위치나 병렬화 방식을 재설계한다. [공식 병렬 처리 문서](https://docs.spring.io/spring-batch/reference/scalability.html#multi-threaded-step)

태스클릿 스텝은 `.tasklet(t, transactionManager)`가 그대로 유효하다. 트랜잭션이 필요 없는 태스클릿은 `.tasklet(t)`로 쓸 수 있다.

검색:
```
rg "\bchunk\([^)]*," src
rg "CompletionPolicy|TimeoutTerminationPolicy|\.completionPolicy\(" src
rg "\.taskExecutor\(|AsyncTaskExecutor|TaskExecutor|MANDATORY|@Transactional" src
```

### 3. 컴포넌트 초기화 변경과 제거된 Reader·Writer

다음 클래스들은 Batch 6.0.6에도 존재하지만 `InitializingBean`을 구현하지 않으므로 해당 타입의 `afterPropertiesSet()` 호출을 제거한다. 생성자·빌더·open 등 실제 검증 시점은 컴포넌트별로 확인하고, 필수 설정이 유지되는지 테스트한다.

- `FlatFileItemReader`
- `AbstractCursorItemReader` (`JdbcCursorItemReader`, `StoredProcedureItemReader` 포함)
- `JpaItemWriter`
- `MongoItemWriter`, `JmsItemReader`, `SimpleMailMessageItemWriter`
- `SynchronizedItemStreamReader`, `SynchronizedItemStreamWriter`
- `PatternMatchingCompositeLineMapper`, `PatternMatchingCompositeLineTokenizer`, `BeanWrapperFieldExtractor`

`JdbcBatchItemWriter`, `JdbcPagingItemReader`, `FlatFileItemWriter`, `CompositeItemWriter`는 여전히 `InitializingBean`이다.

`<T extends InitializingBean>` 같은 제네릭 헬퍼로 여러 컴포넌트를 한꺼번에 초기화하던 코드는 위 클래스를 인자로 넘기는 호출부만 컴파일 에러가 난다. 해당 호출만 제거한다.

다음 클래스는 **클래스 자체가 제거**됐으므로 초기화 호출이나 import만 바꿔서는 해결되지 않는다.

| 제거된 클래스 | 검토할 대체 |
|---|---|
| `MongoItemReader`, `MongoItemReaderBuilder` | 기존 읽기 방식에 따라 `MongoPagingItemReader` 또는 `MongoCursorItemReader`와 대응 빌더 검토 |
| `Neo4jItemReader`, `Neo4jItemWriter` | 목표 버전의 Neo4j 접근 API를 사용하는 Reader·Writer 구현 등 별도 대체 설계 |

대체 시 쿼리·정렬·페이징/커서·저장 상태·재시작 의미를 보존한다. 이 변경은 4→6에도 적용된다. [공식 제거 API 목록](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide#removed-apis)

검색:
```
rg "\.afterPropertiesSet\(\)" src
rg "extends InitializingBean" src
rg "MongoItemReader|MongoItemReaderBuilder|Neo4jItemReader|Neo4jItemWriter" src
```

### 4. `JobLauncher` / `JobLauncherTestUtils` → `JobOperator` / `JobOperatorTestUtils`

`JobLauncher` 인터페이스와 `TaskExecutorJobLauncher` 구현이 모두 `@Deprecated`. `JobOperator`로 통합됐다.

| 이전 | 변경 후 |
|---|---|
| `JobLauncher` 인터페이스 주입 | `JobOperator` 인터페이스 주입 |
| `new TaskExecutorJobLauncher()` | 자동 구성된 JobOperator 주입 또는 `TaskExecutorJobOperator` 직접 구성. 아래 필수 의존성·초기화 확인 |
| `JobLauncher.run(Job, JobParameters)` | `JobOperator.start(Job, JobParameters)` |
| `JobLauncherTestUtils.launchJob(...)` | `JobOperatorTestUtils.startJob(...)` |
| `JobLauncherTestUtils.launchStep(String)` | `JobOperatorTestUtils.startStep(String)` |
| `StepBuilder.job(Job).launcher(...)` | `StepBuilder.job(Job).operator(...)` |

**직접 구성하는 실행기**: 자동 구성된 JobOperator를 주입받는 경우와 직접 `new`로 생성하는 경우를 구분한다. `TaskExecutorJobOperator`에는 `JobRepository`뿐 아니라 **JobRegistry도 필요**하다. 기존 launcher에 repository만 설정하던 코드를 옮기면 초기화 시 `JobLocator must be provided` 오류가 발생할 수 있다.

- 직접 구성할 때 repository·registry를 연결하고, registry에 대상 잡이 등록되는지 확인한다. 시작뿐 아니라 registry 조회가 필요한 재시작·중지 경로도 검증한다.
- 기존 동기/비동기 실행 의미에 맞는 TaskExecutor를 유지한다. 비동기이면 `start(...)`의 반환을 잡 완료로 판단하지 않는다.
- Spring 빈이면 컨테이너의 초기화 콜백을 사용하고, 컨테이너 밖에서 생성하면 필수 설정 후 `afterPropertiesSet()`을 호출한다. 3절의 호출 제거는 그 절에 해당하는 컴포넌트에 한정된다.
- 기존 실행기에 트랜잭션 프록시가 필요했다면 `JobOperatorFactoryBean` 등의 구성을 검토해 유지한다. 직접 생성한 객체에 프록시가 자동으로 생긴다고 가정하지 않는다.

근거: [TaskExecutorJobOperator API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/launch/support/TaskExecutorJobOperator.html).

`JobOperator` 본체 메서드도 도메인 객체 기반으로 바뀌었다. 문자열·`long` ID를 받던 메서드는 **전부** deprecated다.

| 이전 (deprecated) | 변경 후 |
|---|---|
| `operator.start(String jobName, Properties)` | `operator.start(Job, JobParameters)` |
| `operator.stop(long executionId)` | `operator.stop(JobExecution)` |
| `operator.restart(long executionId)` | `operator.restart(JobExecution)` |
| `operator.abandon(long executionId)` | `operator.abandon(JobExecution)` |
| `operator.startNextInstance(String jobName)` | `operator.startNextInstance(Job)` |
| `operator.getRunningExecutions(String)` → `Set<Long>` | `JobRepository.findRunningJobExecutions(String)` → `Set<JobExecution>` |
| `operator.getJobNames()` | `JobRegistry.getJobNames()` |
| `operator.getJobInstances(String, int, int)`, `getExecutions(long)`, `getJobInstance(String, JobParameters)` | `JobRepository`의 조회 메서드 |
| `operator.getParameters(long)`, `getSummary(long)`, `getStepExecutionSummaries(long)` | `JobRepository.getJobExecution(long)`으로 `JobExecution`을 얻어 직접 조합 |

**테스트 유틸.** `JobOperatorTestUtils`에는 기본 생성자가 없다. 생성자는 `(JobOperator, JobRepository)`뿐이라 `new JobLauncherTestUtils()` 뒤에 setter를 부르던 테스트는 그대로 옮길 수 없다. `@SpringBatchTest`를 붙이고 주입받는 방식이 가장 짧다.

```java
// Batch 5
JobLauncherTestUtils testUtils = new JobLauncherTestUtils();

@BeforeEach
void setUp(@Autowired JobRepository jobRepository,
           @Autowired JobLauncher jobLauncher,
           @Autowired Job sampleJob) {
    testUtils.setJobRepository(jobRepository);
    testUtils.setJobLauncher(jobLauncher);
    testUtils.setJob(sampleJob);
}

// Batch 6
@SpringBootTest
@SpringBatchTest
class SampleJobTest {
    @Autowired JobOperatorTestUtils jobOperatorTestUtils;
    @Autowired Job sampleJob;

    @BeforeEach
    void setUp() {
        jobOperatorTestUtils.setJob(sampleJob);
    }
}
```

검색:
```
rg "JobLauncherTestUtils|launchStep\(|launchJob\(|setJobLauncher\(" src
rg "TaskExecutorJobLauncher|\.launcher\(" src
rg "\bJobLauncher\b" src
rg "operator\.(getJobNames|getRunningExecutions|getJobInstances|getExecutions|getParameters|getSummary|getStepExecutionSummaries)\(" src
rg "operator\.(stop|restart|abandon)\(\s*[0-9a-zA-Z_]+\s*\)" src
rg "operator\.(startNextInstance|start)\(\s*\"" src
```

### 5. `JobRepository`가 `JobExplorer`를 흡수

Batch 6.0부터 `JobRepository extends JobExplorer`. 두 인터페이스를 각각 주입받던 코드는 `JobRepository` 하나로 통일한다. `JobExplorer` 자체는 1절대로 `core.repository.explore`로 이동했다.

검색:
```
rg "\bJobExplorer\b" src
```

### 6. Spring Boot 4의 배치 자동 구성 패키지 재배치

| 이전 (Boot 3) | 변경 후 (Boot 4) |
|---|---|
| `org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration` | `org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration` 등으로 역할 분리. 제외 설정은 아래 기준으로 재검토 |
| `org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner` | `org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner` |
| `org.springframework.boot.autoconfigure.batch.BatchDataSource` | `org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource` |
| `BatchProperties$Jdbc` 내부 클래스 | `org.springframework.boot.batch.jdbc.autoconfigure.BatchJdbcProperties` |

`JobLauncherApplicationRunner` 생성자가 `(JobLauncher, JobExplorer, JobRepository)` 3-인자에서 `(JobOperator)` 단일 인자로 단순화됐다.

**자동 설정 제외는 클래스명 치환만으로 끝내지 않는다.** Boot 4에서는 다음 역할이 분리됐다.

| 자동 설정 | 역할 |
|---|---|
| `org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration` | 저장소별 구성이 없을 때 기본 resourceless 기반 구성 |
| `org.springframework.boot.batch.jdbc.autoconfigure.BatchJdbcAutoConfiguration` | JDBC 기반 구성과 관련 스키마 초기화 |
| `org.springframework.boot.batch.autoconfigure.BatchJobLauncherAutoConfiguration` | 기동 시 잡을 실행하는 Runner 등 |

기존 `exclude = BatchAutoConfiguration.class` 또는 `spring.autoconfigure.exclude` 사용처는 왜 제외했는지 확인하고 새 구성에서도 같은 동작이 되는지 검증한다. 모든 자동 설정을 일괄 제외하지 않는다. 기동 시 잡 실행만 막으려는 목적이라면 `spring.batch.job.enabled=false`를 검토한다.

`@EnableBatchProcessing`이나 명시적 기반 구성 때문에 저장소 자동 설정이 물러나도, JobOperator 빈이 있으면 잡 실행 자동 설정은 별도로 활성화될 수 있다. `spring.batch.*` 전체가 무시된다고 가정하지 말고 기반 구성·스키마 초기화·잡 자동 실행을 각각 확인한다. 기존 수동 실행 코드와 Runner가 잡을 중복 실행하지 않는지 실제 기동으로 검증한다. 수동 스키마 구성이면 초기화 주체도 명시한다.

근거: [BatchJdbcAutoConfiguration API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/batch/jdbc/autoconfigure/BatchJdbcAutoConfiguration.html), [BatchJobLauncherAutoConfiguration API](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/batch/autoconfigure/BatchJobLauncherAutoConfiguration.html).

검색:
```
rg "org\.springframework\.boot\.autoconfigure\.batch\." src
rg "new JobLauncherApplicationRunner\([^)]*," src
rg "BatchProperties\s*\.\s*class|properties\.getJdbc\(\)" src
rg "BatchAutoConfiguration|BatchJdbcAutoConfiguration|BatchJobLauncherAutoConfiguration|spring\.autoconfigure\.exclude|spring\.batch\.job\.enabled" src
```

7절 이후에도 컴파일·기동 복구에 필요한 변경이 있다. 프로젝트에 해당하는 절까지 함께 적용한 뒤 빌드·테스트를 실행한다. 검증 시점은 위의 적용 순서를 따른다.

### 7. 재시도 정책: spring-retry → Spring Framework 7 `RetryPolicy`

이 절은 2절의 청크 빌더 교체와 한 묶음이다.

- 옛 경로(`.chunk(int, tm)` → `FaultTolerantStepBuilder`)는 Batch 6에서도 spring-retry의 `RetryPolicy`와 `BackOffPolicy`만 받는다.
- 새 경로(`.chunk(int)` → `ChunkOrientedStepBuilder`)는 `faultTolerant()` 뒤에 `retryPolicy(org.springframework.core.retry.RetryPolicy)`, `retry(Class...)`, `retryLimit(long)`, `retryListener(org.springframework.core.retry.RetryListener)`를 받는다. spring-retry 타입은 넘길 수 없다.
- spring-retry는 Batch 6에서도 infrastructure 모듈의 compile 의존성이라, 옛 정책 클래스는 그대로 컴파일된다. 남아 있는 사용처는 아래 검색 패턴으로만 잡힌다.

| 이전 (`spring-retry`) | 변경 후 (Spring Framework 7 `RetryPolicy.builder()`) |
|---|---|
| `new SimpleRetryPolicy(maxAttempts, Map.of(Ex.class, true))` | `maxAttempts >= 1`일 때 `.maxRetries(maxAttempts - 1).includes(Ex.class)`. 기존 backoff도 함께 명시 |
| `new TimeoutRetryPolicy()` + `setTimeout(ms)` | `.timeout(Duration.ofMillis(ms))`와 횟수·대기 시간을 함께 설정. timeout만 지정하면 기본 재시도 제한이 남는다 |
| `new FixedBackOffPolicy()` + `setBackOffPeriod(ms)` | `.delay(Duration.ofMillis(ms))` |
| `new ExponentialBackOffPolicy()` + `setInitialInterval/Multiplier/MaxInterval` | `.delay(...).multiplier(...).maxDelay(...)` |
| 커스텀 `BackOffPolicy` | `.backOff(org.springframework.util.backoff.BackOff)`. 횟수·대기 시간은 이 BackOff에서 관리하며 아래 주의점 확인 |
| `FaultTolerantStepBuilder.retryLimit(n)` | 기본 정책이고 `n >= 1`이면 새 빌더의 `retryLimit(n - 1)`로 총 시도 횟수 보존. 대기 시간은 별도 RetryPolicy로 명시 |
| `FaultTolerantStepBuilder.listener(org.springframework.retry.RetryListener)` | `ChunkOrientedStepBuilder.retryListener(org.springframework.core.retry.RetryListener)` |

`SimpleRetryPolicy`의 `maxAttempts`는 첫 시도를 포함한 총 횟수이고, `maxRetries`는 첫 시도를 제외한 재시도 횟수다. 옛 빌더는 `retryLimit`을 SimpleRetryPolicy의 총 시도 횟수로 전달하지만 새 빌더는 `maxRetries`로 전달한다. 별도 정책이 없는 구성에서 기존 `.retryLimit(3)`을 새 빌더에 그대로 옮기면 총 3회가 총 4회로 늘어난다. 0·1은 별도로 테스트하며, 0에 무조건 1을 빼서 음수를 만들지 않는다. 커스텀 정책을 쓰면 그 정책이 횟수를 결정하므로 위 산식을 기계적으로 적용하지 않는다.

Spring Framework RetryPolicy 빌더의 기본값은 최대 재시도 3회와 대기 1초다. 기존에 backoff가 없었다면 `.delay(Duration.ZERO)`로 명시한다. `.timeout(...)`만 설정해도 기본 횟수 제한이 적용되므로 시간만으로 제한하던 정책과 동등하지 않다. 기존 종료 조건을 확인해 횟수 제한과 timeout을 함께 정한다. `timeout` API는 Framework 7.0.2부터 제공되므로 실제 Framework 버전도 확인한다.

커스텀 `.backOff(...)`를 지정할 때는 `.maxRetries(...)`, `.delay(...)`, `.multiplier(...)`, `.maxDelay(...)` 등과 혼합하지 않는다. 해당 BackOff의 중단 조건과 대기 시간을 검증한다. 명시적인 `.retryPolicy(...)`를 사용하는 경우 새 스텝 빌더의 `.retryLimit(...)`와 `.retry(...)`가 정책에 추가로 합쳐진다고 가정하지 않는다.

표는 설정 항목의 대응이며 정책 전체의 동등성을 보장하지 않는다. 총 시도 횟수, timeout과 횟수 제한의 결합, 예외 분류·cause 탐색, backoff, 리스너 호출을 실제 실패 입력으로 검증한다. Batch 밖에서 사용하는 spring-retry는 별도 사용처로 분류하고 일괄 삭제하지 않는다.

```java
// Batch 5: .retry(TransientDataAccessException.class).retryLimit(3), backoff 미설정
// Batch 6.0.6: 최초 1회 + 재시도 2회, 대기 없음으로 기존 정책을 보존
new StepBuilder("step", jobRepository)
    .<I, O>chunk(10)
    .transactionManager(transactionManager)
    .reader(r).writer(w)
    .faultTolerant()
    .retryPolicy(RetryPolicy.builder()
        .maxRetries(2)
        .includes(TransientDataAccessException.class)
        .delay(Duration.ZERO)
        .build())
    .build();
```

import: `org.springframework.retry.RetryPolicy` → `org.springframework.core.retry.RetryPolicy`

근거: [ChunkOrientedStepBuilder 구현](https://github.com/spring-projects/spring-batch/blob/6.0.x/spring-batch-core/src/main/java/org/springframework/batch/core/step/builder/ChunkOrientedStepBuilder.java), [RetryPolicy.Builder API](https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/core/retry/RetryPolicy.Builder.html). 링크의 브랜치·문서는 갱신될 수 있으므로 실제 목표 버전과 대조한다.

검색:
```
rg "org\.springframework\.retry\." src
rg "TimeoutRetryPolicy|SimpleRetryPolicy|FixedBackOffPolicy|ExponentialBackOffPolicy|CompositeRetryPolicy|BinaryExceptionClassifierRetryPolicy|MaxAttemptsRetryPolicy|CircuitBreakerRetryPolicy" src
rg "\.backOffPolicy\(|\.retryPolicy\(|\.retryLimit\(" src
```

### 8. 리스너 인터페이스와 호출 시점 변화

아래 `ItemWriteListener`의 List→Chunk 변경은 4→5에서 발생했다. Batch 5에서는 이미 Chunk를 사용하므로 해당 행의 추가 변경은 필요 없다.

| 이전 | 변경 후 |
|---|---|
| `ItemWriteListener.beforeWrite(List<? extends S>)` | `beforeWrite(Chunk<? extends S>)` |
| `ItemWriteListener.afterWrite(List<? extends S>)` | `afterWrite(Chunk<? extends S>)` |
| `ItemWriteListener.onWriteError(Exception, List<? extends S>)` | `onWriteError(Exception, Chunk<? extends S>)` |
| `ChunkListener.beforeChunk(ChunkContext)` (deprecated) | `beforeChunk(Chunk<I>)` |
| `ChunkListener.afterChunk(ChunkContext)` (deprecated) | `afterChunk(Chunk<O>)` |
| `ChunkListener.afterChunkError(ChunkContext)` (deprecated) | `onChunkError(Exception, Chunk<O>)` |
| `@AfterChunkError` | `@OnChunkError` |
| `ChunkListener.ROLLBACK_EXCEPTION_KEY` | deprecated. `onChunkError`의 첫 인자로 예외를 직접 받는다 |
| `extends JobExecutionListenerSupport` | `implements JobExecutionListener` |
| `extends StepExecutionListenerSupport` | `implements StepExecutionListener` |
| `extends ChunkListenerSupport` | `implements ChunkListener<I, O>` |
| `extends SkipListenerSupport` | `implements SkipListener<T, S>` |

`ChunkListener`는 제네릭 타입 `ChunkListener<I, O>`로 바뀌었다. `ChunkContext`를 받는 옛 메서드 셋은 default 메서드로 남아 있지만 deprecated라 `-Werror` 빌드에서 걸린다. `JobExecutionListenerSupport`, `StepExecutionListenerSupport`, `ChunkListenerSupport`, `SkipListenerSupport`는 클래스 자체가 제거됐다. 여러 리스너를 한 클래스에 모으고 싶으면 `ItemListenerSupport`, `StepListenerSupport`는 그대로 남아 있으니 이쪽을 쓴다.

**호출 시점은 동등하지 않다.** 새 청크 모델로 옮기면 아래 차이까지 검토한다.

| 콜백 | 기존 ChunkContext 기반 | 새 Chunk 기반 |
|---|---|---|
| `beforeChunk` | 청크 실행 전, 트랜잭션 안 | 읽기가 끝난 뒤 처리 전, 트랜잭션 안 |
| `afterChunk` | 청크 실행 후, 트랜잭션 밖 | 쓰기가 끝난 뒤, 트랜잭션 안 |
| `afterChunkError` → `onChunkError` | 롤백 후 | 처리·쓰기 오류 시 롤백 예정인 트랜잭션 안 |

새 콜백은 동시 실행 스텝에서 호출되지 않는다는 제약도 있다. 감사 기록·파일 이동·외부 통지·실행 컨텍스트 갱신을 수행하는 리스너는 단순히 파라미터만 바꾸지 않는다. 커밋 성공 후 실행해야 하는 로직인지, 오류 기록이 롤백되어도 되는지, 별도 트랜잭션이 필요한지 확인하고 성공·롤백·병렬 경로를 검증한다. 기존 ChunkContext에서 읽던 정보의 접근 경로도 확인한다.

근거: [ChunkListener API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/listener/ChunkListener.html). 실제 목표 버전의 문서 또는 소스와 대조한다.

검색:
```
rg "beforeWrite\(List<|afterWrite\(List<|onWriteError\([^,]+,\s*List<" src
rg "afterChunkError|ROLLBACK_EXCEPTION_KEY|@AfterChunkError" src
rg "(beforeChunk|afterChunk)\(ChunkContext" src
rg "ChunkListener\b[^<]" src
rg "JobExecutionListenerSupport|StepExecutionListenerSupport|ChunkListenerSupport|SkipListenerSupport" src
```

### 9. `@EnableBatchProcessing`과 `@EnableJdbcJobRepository` / `@EnableMongoJobRepository`

Batch 6.0부터 저장소별 전용 애너테이션이 생겼다. 0절의 스타터 선택과 짝을 이룬다. JDBC와 MongoDB 저장소 애너테이션을 같은 기본 저장소 구성에 함께 붙이지 않는다.

**중요**: 저장소 자동 설정을 사용하는 경우에는 `@EnableBatchProcessing`과 저장소별 애너테이션을 덧붙이지 않는다. 필요한 스타터·데이터소스·트랜잭션 매니저와 속성이 있으면 Boot가 저장소를 구성한다. 명시적 구성으로 전환하면 저장소와 스키마 초기화 속성의 적용 여부를 확인한다. 기동 시 잡 실행 자동 설정은 별개이므로 6절도 함께 적용한다.

애너테이션을 꼭 써야 한다면 `@EnableBatchProcessing`과 **함께** 쓴다 (대체가 아니다). 데이터소스와 저장소 관련 속성은 `@EnableBatchProcessing`에서 빠지고 `@EnableJdbcJobRepository`로 옮겨 갔다.

| 애너테이션 | Batch 6에서의 속성 |
|---|---|
| `@EnableBatchProcessing` | `modular`, `taskExecutorRef`, `jobRegistryRef`, `observationRegistryRef`, `transactionManagerRef`, `jobParametersConverterRef` |
| `@EnableJdbcJobRepository` | `dataSourceRef`, `transactionManagerRef`, `jdbcOperationsRef`, `tablePrefix`, `isolationLevelForCreate`, `databaseType`, `validateTransactionState`, `charset`, `maxVarCharLength`, `clobType`, `jobKeyGeneratorRef`, `executionContextSerializerRef`, `incrementerFactoryRef`, `conversionServiceRef` |

같은 이름의 `transactionManagerRef`라도 적용 대상이 다르다.

| 설정 위치 | 적용 대상 |
|---|---|
| `@EnableBatchProcessing(transactionManagerRef = ...)` | JobOperator |
| `@EnableJdbcJobRepository(transactionManagerRef = ...)` | JobRepository |
| 스텝 빌더의 `.transactionManager(...)` 또는 `.tasklet(tasklet, tm)` | 스텝 업무 처리 |

메타 DB와 업무 DB가 다르면 메타 저장소의 데이터소스·트랜잭션 매니저를 짝으로 지정하고, 스텝에는 업무 DB의 트랜잭션 매니저를 지정한다. 아래 `batchTransactionManager`는 `batchDataSource`를 대상으로 생성한 빈이어야 한다. JobOperator의 트랜잭션 설정은 실행 방식에 맞춰 따로 정한다.

```java
@EnableBatchProcessing(taskExecutorRef = "batchTaskExecutor")
@EnableJdbcJobRepository(
    dataSourceRef = "batchDataSource",
    transactionManagerRef = "batchTransactionManager",
    tablePrefix = "BATCH6_")
class MyJobConfiguration { ... }
```

근거: [EnableBatchProcessing API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/configuration/annotation/EnableBatchProcessing.html), [EnableJdbcJobRepository API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/configuration/annotation/EnableJdbcJobRepository.html).

`isolationLevelForCreate` 값 타입이 `String` → `Isolation` enum.

| 이전 | 변경 후 |
|---|---|
| `@EnableBatchProcessing(dataSourceRef = "ds", isolationLevelForCreate = "ISOLATION_REPEATABLE_READ")` | `@EnableJdbcJobRepository(dataSourceRef = "ds", isolationLevelForCreate = Isolation.REPEATABLE_READ)` |

`DefaultBatchConfiguration` 상속 방식이면 `JdbcDefaultBatchConfiguration`으로 슈퍼클래스를 바꾼다. Batch 6의 `DefaultBatchConfiguration`은 메타데이터를 저장하지 않는 `ResourcelessJobRepository`를 만든다. 재시작·이력·중복 실행·파티셔닝을 검증하는 테스트에는 JDBC 등 해당 기능을 지원하는 저장소를 사용한다.

몽고DB 저장소 주의점 (해당 스타터를 제공하는 Boot 4.1 이상을 목표로 할 때):
- `spring.batch.data.mongodb.schema.initialize=true`로 컬렉션 초기화
- 트랜잭션을 위해 반드시 Replica set(단일 노드라도)으로 띄움
- 테스트는 `MongoDBContainer` + `@ServiceConnection`

검색:
```
rg "@EnableBatchProcessing\([^)]*(dataSourceRef|isolationLevelForCreate|tablePrefix)" src
rg "isolationLevelForCreate\s*=\s*\"ISOLATION_" src
rg "extends DefaultBatchConfiguration\b" src
rg "@EnableBatchProcessing|@EnableJdbcJobRepository|@EnableMongoJobRepository" src
```

### 10. 배치 관련 스프링 부트 속성명 변화

자동 설정으로 쓰는 프로젝트에서는 **속성명 변경이 실질적인 업그레이드 작업**이다.
Boot 3 프로젝트에는 Boot 2.5와 3.0의 속성 변경(`spring.batch.jdbc.*`로의 이동, `spring.batch.job.name` 단일화)이 이미 반영돼 있을 것이다. 아직 옛 이름이 남아 있다면 함께 고친다.

| 속성 | 변경 | 반영 버전 |
|---|---|---|
| `spring.batch.jdbc.validate-transaction-state` | 신규. 기본값 `true` | Boot 4.0 |
| `spring.batch.data.mongodb.*` | 신규 | Boot 4.1 |
| `spring.batch.initialize-schema`, `spring.batch.schema`, `spring.batch.table-prefix`, `spring.batch.isolation-level-for-create` | `spring.batch.jdbc.*`로 이동 | Boot 2.5 (남아 있으면 정리) |
| `spring.batch.job.names` (콤마 구분) | `spring.batch.job.name` (단일 값) | Boot 3.0 (남아 있으면 정리) |

일괄 진단에는 `spring-boot-properties-migrator` 모듈이 유용하다. `build.gradle`에 런타임 의존성으로 추가하면 부팅 시 옛 속성명과 권장 대체를 로그로 출력한다.

```groovy
dependencies {
    runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'
}
```

검증이 끝나면 이 의존성은 제거한다. 배치 외 속성 변경은 아래 공식 가이드를 참고한다.

- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide

검색:
```
rg "spring\.batch\.(initialize-schema|schema|table-prefix|isolation-level-for-create)\b" src
rg "spring\.batch\.job\.names\b" src
```

### 11. `modular=true` / `ApplicationContextFactory` 패턴 폐기

아래 단일 컨텍스트 대체는 기존 컨텍스트 격리가 필요 없는 경우에만 적용한다. 같은 이름의 빈, 잡별 프로퍼티·데이터소스·빈 가시성을 먼저 조사한다. 격리 의미가 필요하면 목표 버전의 컨텍스트 계층 및 잡 그룹 구성 방식을 검토한다. 빈 덮어쓰기 허용으로 충돌을 감추지 않는다.

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

### 12. 도메인 객체의 생성자 정리

| 이전 | 변경 후 |
|---|---|
| `new JobExecution(Long)` 단일 인자 | 제거. `new JobExecution(long, JobInstance, JobParameters)` |
| `new StepExecution(String, JobExecution)` | deprecated. `new StepExecution(long, String, JobExecution)` |
| `new StepExecution(String, JobExecution, Long)` | `new StepExecution(long, String, JobExecution)` — 인자 순서 변경 |

테스트에서는 가능하면 생성자 대신 `MetaDataInstanceFactory`를 쓴다.

| 대신 | 이렇게 |
|---|---|
| `new JobExecution(0L, params)` | `MetaDataInstanceFactory.createJobExecution("testJob", 0L, 0L, params)` |
| `new JobExecution(0L)` | `MetaDataInstanceFactory.createJobExecution()` |
| `new StepExecution("name", jobExec)` | `MetaDataInstanceFactory.createStepExecution(jobExec, "name", 0L)` |

검색:
```
rg "new JobExecution\(|new StepExecution\(" src
```

### 13. Jackson 2 → Jackson 3 (`tools.jackson`)

| 이전 | 변경 후 |
|---|---|
| `new JacksonJsonObjectReader<>(objectMapper, Clazz.class)` | `new JacksonJsonObjectReader<>(Clazz.class)` 또는 `(jsonMapper, Clazz.class)` |
| `new JacksonJsonObjectMarshaller<>(objectMapper)` | `new JacksonJsonObjectMarshaller<>()` 또는 `(jsonMapper)` |
| `com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.json.JsonMapper` |
| `registerModule(new JavaTimeModule())` | JSR-310 내장 |
| `jackson-datatype-jsr310` 의존성 | Jackson 3 사용처에는 불필요. 다른 Jackson 2 사용처가 없는지 확인한 뒤 제거 |
| `FAIL_ON_UNKNOWN_PROPERTIES` 기본 true | 기본 false |

Batch 6의 `JacksonJsonObjectReader`와 `JacksonJsonObjectMarshaller`는 Jackson 3 전용이다. Jackson 2 `ObjectMapper`를 넘기는 생성자는 없다.

`com.fasterxml.jackson.annotation.*`와 `jackson-annotations`는 Jackson 3에서도 유지된다. 검색에 걸렸다는 이유로 `tools.jackson.annotation.*`로 바꾸지 않는다. 반면 databind 모듈의 애너테이션은 이동 대상이므로 실제 소속을 확인한다. [Jackson 공식 마이그레이션 문서](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)

기존 ObjectMapper의 커스텀 모듈·날짜 형식·명명 전략·알 수 없는 필드 처리 설정을 조사한다. 인자 없는 생성자로 바꿔 설정을 유실하지 말고, 동일 JSON 입력·출력으로 동등성을 검증한다. ExecutionContext serializer 변경은 16절의 기존 데이터 읽기 검증과 함께 처리한다.

검색:
```
rg "com\.fasterxml\.jackson" src
rg "JavaTimeModule|jackson-datatype-jsr310|Jackson2ObjectMapperBuilder" src
```

### 14. Spring Framework 7의 deprecation: JSpecify, `PathResource`

| Spring 6 | Spring 7 |
|---|---|
| `org.springframework.lang.Nullable` | `org.jspecify.annotations.Nullable` |
| `org.springframework.lang.NonNull` | `org.jspecify.annotations.NonNull` |
| `org.springframework.core.io.PathResource` | `org.springframework.core.io.FileSystemResource` (`Path`를 받는 생성자가 있다) |

검색:
```
rg "org\.springframework\.lang\.(Nullable|NonNull)" src
rg "\bPathResource\b" src
```

### 15. `JobParameters` 구조·실행 의미 변경과 타입별 getter

**필수 API 변경**: Batch 6의 `JobParameter<T>`는 이름을 포함한 record다. `JobParameters`는 Map 대신 Set을 보유한다. 직접 생성·순회·직렬화하는 코드와 테스트를 확인한다.

| Batch 5 | Batch 6 |
|---|---|
| 이름을 Map 키로 별도 보관하는 `JobParameter` | `new JobParameter<>(name, value, type, identifying)` |
| `getValue()`, `getType()`, `isIdentifying()` | record 접근자 `value()`, `type()`, `identifying()`; 이름은 `name()` |
| `new JobParameters(map)` | `JobParametersBuilder`로 이름·타입·identifying을 보존해 구성하거나 `Set<JobParameter<?>>`로 변환 |
| `getParameters()`의 Map 순회 | Set 순회 및 각 파라미터의 `name()` 사용 |

**incrementer 사용처**: 잡에 incrementer가 설정되어 있으면 Batch 6에서는 다음 인스턴스 파라미터를 프레임워크가 계산하며 추가로 전달한 파라미터는 경고와 함께 무시된다. `RunIdIncrementer`, `.incrementer(...)`, `startNextInstance(...)`와 실행 진입점을 함께 확인한다. 실제 스크립트로 전달한 업무일자·파일경로가 최종 JobExecution에 들어가는지 검증한다. incrementer를 임의로 제거하거나 유지하지 말고 기존 실행 의도에 맞춰 구성한다.

**명령행 계약**: 기본 변환기의 `key=value,type,identifying`에서 type과 identifying은 선택 사항이며 기본값은 String과 true다. 기존 String 파라미터의 `key=value`는 유효하다. 이름·타입·값·identifying을 보존하고, 타입 변경을 별도 리팩터링으로 분류한다. 사용자 정의 타입은 문자열과의 양방향 변환, 저장 후 조회 및 재시작 시 복원을 검증한다. 사용하는 변환기가 기본 구현인지도 확인한다.

**선택적 개선**: `ChunkContext.getStepContext().getJobParameters()`의 Map 접근은 그 자체로 필수 제거 대상이 아니다. 타입별 접근이 유리하면 `StepContribution.getStepExecution().getJobParameters()`의 getter를 사용한다. 아래 예제는 기존 파라미터가 이미 LocalDate/Long인 경우다.

```java
// Map 기반
Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
LocalDate baseDate = (LocalDate) params.get("baseDate");

// Batch 6 권장
JobParameters params = contribution.getStepExecution().getJobParameters();
LocalDate baseDate = params.getLocalDate("baseDate");
Long chunkSize = params.getLong("chunkSize");
```

검색:
```
rg "getStepContext\(\)\.getJobParameters\(\)" src
rg "new JobParameter|new JobParameters|Map<.*JobParameter|getParameters\(\)|getValue\(\)|getType\(\)|isIdentifying\(\)" src
rg "RunIdIncrementer|JobParametersIncrementer|\.incrementer\(|startNextInstance\(" src
```

근거: [JobParameter API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/job/parameters/JobParameter.html), [JobParameters API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/job/parameters/JobParameters.html), [기본 파라미터 변환기 API](https://docs.spring.io/spring-batch/reference/api/org/springframework/batch/core/converter/DefaultJobParametersConverter.html), [6.0 가이드의 파라미터 변경](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide#changes-related-to-job-parameters).

### 16. 메타DB·실행 이력·ExecutionContext 전환

이 절은 사전 조사에서 먼저 읽는다. 코드의 컴파일 성공과 기존 메타데이터 호환성은 별개다.

- **스키마**: 6.0에서는 `BATCH_JOB_SEQ`가 `BATCH_JOB_INSTANCE_SEQ`로 바뀐다. 목표 버전의 `spring-batch-core`에 포함된 `org/springframework/batch/core/migration/6.0/`와 DB별 schema SQL을 확인한다. DB 종류·기존 스키마·접두어에 맞춰 차이를 검토하고, 지원되지 않는 DDL이나 이미 적용된 변경을 그대로 실행하지 않는다.
- **기존 실패 실행**: 공식 6.0 가이드는 파라미터 직렬화 변경으로 v5에서 시작한 실패 인스턴스를 v6에서 재시작할 수 없다고 명시한다. 전환 전에 기존 버전에서 성공시킬지, 업무 판단에 따라 abandon하고 별도 재처리할지 결정한다. 에이전트가 임의로 실행 상태를 바꾸지 않는다. 실행 중인 잡과 스케줄러의 전환 시점도 정한다.
- **ExecutionContext**: 기존 serializer, 저장된 객체 타입과 클래스명, 새 버전의 serializer를 조사한다. Jackson 2→3 및 패키지 이동을 포함해 과거 이력·컨텍스트 읽기를 복제 데이터로 확인한다. 읽기 성공을 과거 실패 실행의 재시작 지원으로 해석하지 않는다.
- **이력 유지 또는 저장소 분리**: 기존 스키마를 이행할지, 새 접두어/스키마로 분리할지 요구에 맞춰 결정한다. 분리하면 과거 완료 이력과 체크포인트가 자동으로 이어지지 않으며, 구·신 저장소 사이의 중복 실행 방지도 보장되지 않는다. 스케줄러 전환과 업무 데이터의 멱등성·재처리 범위를 확인한다.
- **접두어와 DDL**: `table-prefix`는 생성·변환 SQL을 대신하지 않는다. 새 접두어를 선택하면 테이블·시퀀스와 필요한 제약조건/인덱스를 포함한 DDL을 준비하고, 실제 활성 구성 한 곳에 같은 접두어를 지정한다. 스키마 초기화와 기존 데이터 마이그레이션을 구분한다.
- **리허설·롤백**: 운영 DB와 같은 종류의 격리된 DB 및 허용된 복제 데이터로 DDL, 이력 조회, 새 실행·실패 후 재시작을 검증한다. 메타DB 백업·복원뿐 아니라 전환 후 변경된 업무 데이터와 외부 부작용의 처리까지 계획한다. 코드나 접두어만 되돌리면 전체 작업이 롤백된다고 간주하지 않는다.

검색 예시 (소스 외 SQL, 배포 설정, 운영 스크립트도 확인):
```
rg --hidden -n -g '!.git' -g '!**/build/**' -g '!**/target/**' -g '!**/.gradle/**' 'BATCH_JOB_SEQ|BATCH_JOB_INSTANCE_SEQ|table-prefix|tablePrefix|ExecutionContextSerializer|executionContextSerializer|initialize-schema' .
```

근거: [6.0 공식 가이드](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide#historical-data-access-implications), [6.0.5 PostgreSQL 마이그레이션 SQL 예시](https://github.com/spring-projects/spring-batch/blob/v6.0.5/spring-batch-core/src/main/resources/org/springframework/batch/core/migration/6.0/migration-postgresql.sql). 실제 적용에는 목표 버전과 해당 DB의 파일을 사용한다.

### 17. 메트릭·추적·운영 알림 보존

모니터링을 사용하지 않는 프로젝트는 해당 없음으로 기록한다. 사용한다면 컴파일과 업무 출력 검증 외에 메트릭·추적·실패 알림도 확인한다.

- Batch 6에서는 Micrometer의 전역 정적 registry 사용이 제거됐다. 배치가 실제 사용하는 `ObservationRegistry`와 관측 핸들러·MeterRegistry 연결을 확인한다. Boot/Actuator 자동 설정이 이미 제공하는 registry를 중복 생성하지 않는다.
- 수동 JobOperator·Job·Step 구성에서는 registry가 NOOP으로 남거나 연결이 빠지지 않았는지 확인한다. 설정 파일에 빈이 있다는 사실만으로 수집 성공을 판단하지 않는다.
- 성공·실패 잡을 실행한 뒤 실제 수집 대상에서 잡·스텝 메트릭과 사용 중인 추적 데이터가 생성되는지 확인한다. 기존 대시보드와 알림이 참조하는 메트릭명·태그·잡 이름을 대조한다. 4→6에서는 4→5의 Micrometer 태그 변경도 적용 대상이다.
- 실패·지연·미실행 알림을 운영에서 사용한다면 격리된 검증 환경에서 해당 조건을 재현해 규칙이 동작하는지 확인한다. 운영 알림을 실제로 발송할지는 해당 작업의 권한과 검증 계획에 따른다.

검색:
```
rg "ObservationRegistry|MeterRegistry|Metrics\.globalRegistry|spring\.batch\.(job|step)|management\.(observations|metrics|tracing)" src
```
대시보드·알림 규칙과 배포 설정도 검색 범위에 포함한다.

근거: [6.0 관측 기능 변경](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide#observability-changes), [5.0 관측 기능 변경](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide#observability-updates).

## 완료 후 검수 포인트

'완료 보고 형식'의 증거와 다음 항목을 대조한다. 사용하지 않는 기능은 해당 없음으로, 실행하지 못한 검증은 미검증으로 구분한다.

- 전체 대상 모듈의 빌드·테스트와 deprecation 검사가 통과했는지. 새 경고 억제·테스트 비활성화·테스트 삭제로 실패를 감추지 않았는지.
- 동일 입력의 업무 출력 및 read/write/filter/skip 건수가 기준 결과와 일치하는지. 차이가 있으면 근거와 업무상 허용 여부가 기록됐는지.
- 실제 활성 프로파일의 저장소·데이터소스·트랜잭션 매니저가 의도한 것인지. JDBC면 실제 접두어의 테이블에 새 실행과 스텝·파라미터·컨텍스트가 저장되는지. MongoDB면 해당 컬렉션에서 확인했는지.
- 새 버전에서 실패를 유발한 뒤 재시작하여 체크포인트와 업무 출력의 중복·누락을 검증했는지. 동일 identifying 파라미터의 완료 잡 재실행 및 동시 실행 처리가 기존 의도와 일치하는지. 매번 임의 파라미터를 추가해 중복 방지 검증을 우회하지 않았는지.
- 청크 모델 교체 후 재시도 총 횟수·대기 시간·timeout·skip·롤백·리스너 호출 시점·병렬 처리가 의도대로인지. 병렬 Processor의 DB 변경 후 Writer가 실패했을 때 업무 데이터 결과를 확인했는지. Batch의 spring-retry 정책이 새 API로 옮겨졌는지와 Batch 밖 사용처를 구분했는지.
- 실제 운영 스크립트·CI 명령과 프로파일로 실행했는지. 파라미터 타입·값·identifying과 incrementer 결과가 보존되는지. 모든 파라미터에 타입 힌트를 추가했는지를 완료 기준으로 삼지 않는다.
- 저장소 구성은 자동 설정·애너테이션·상속 중 선택한 방식으로 일관되는지. 단순한 우선순위를 가정하지 말고 조건부 자동 설정의 활성 여부와 실제 빈을 확인했는지. 접두어와 준비한 DDL이 일치하는지.
- JMX·관리자 도구의 `JobOperator` 호출, JSON 설정, 컨텍스트 격리 등 변경한 기능의 실행 경로가 검증됐는지. 직접 구성한 JobOperator의 registry·잡 등록·동기/비동기 실행·초기화가 올바른지.
- 자동 설정 제외와 명시적 구성 변경 후 기동 시 잡 자동 실행 여부가 기존 의도와 일치하는지. 수동 실행 코드와 Runner가 중복 실행하지 않는지.
- 모니터링을 사용하면 17절의 실제 메트릭·추적 수집과 대시보드·실패 알림이 유지되는지.
- 16절의 과거 이력·미완료 실행 처리, DB 전환 리허설과 롤백 계획이 기록됐는지. 기존 버전의 실패 실행을 새 버전에서 그대로 재시작할 수 있다고 가정하지 않았는지.

## 공식 참고 자료

- Spring Batch 6.0 Migration Guide: https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide
- Spring Boot 4.0 Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
