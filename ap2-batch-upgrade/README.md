# 부록 B 예제: 메모 잡(memoJob)의 버전별 진화

같은 잡을 스프링 배치 4 / 5 / 6으로 각각 구현했다.
세 프로젝트가 하는 일은 같다.
텍스트 파일을 한 줄씩 읽어서 `memo` 테이블에 저장한다.
파일 위치는 'memoFile' 잡 파라미터로 받는다.

스프링 부트 그레이들 플러그인은 버전이 다르면 한 빌드에 공존할 수 없다.
그래서 세 프로젝트를 하나의 멀티 프로젝트로 묶지 않고 각각 독립 빌드로 두었다.

| 디렉터리 | 스프링 배치 | 스프링 부트 | JDK | 그레이들 |
| --- | --- | --- | --- | --- |
| `memo-batch4` | 4.3 | 2.7.18 | 17 | 7.6.1 |
| `memo-batch5` | 5.2 | 3.5.6 | 21 | 9.1.0 |
| `memo-batch6` | 6.0.6-SNAPSHOT | 4.1.2-SNAPSHOT | 25 | 9.1.0 |

`memo-batch4`는 JDK 17로 실행한다.
그레이들 7.6.1이 실행을 공식 지원하는 JDK는 19까지이고([호환성 표](https://docs.gradle.org/current/userguide/compatibility.html)),
스프링 부트 2.7.18이 지원하는 JDK는 21까지다.
더 높은 JDK로 띄워도 이 예제의 테스트는 통과하지만(JDK 21과 25에서 확인) 지원 범위 밖이라 보장되지 않는다.

```bash
cd memo-batch4 && JAVA_HOME=~/.sdkman/candidates/java/17.0.16-tem ./gradlew test
cd memo-batch5 && ./gradlew test
cd memo-batch6 && ./gradlew test
```

`memo-batch5`와 `memo-batch6`은 그레이들 툴체인으로 컴파일·테스트 JDK를 지정하므로
어떤 JDK로 그레이들을 띄우든 상관없다.

## 버전마다 달라지는 파일

세 프로젝트의 같은 이름 파일을 나란히 열어놓고 비교하면 변경점이 한눈에 보인다.

- `MemoComponents`: 리더와 라이터를 만든다.
  배치 6에서 `org.springframework.batch.infrastructure.*`로 패키지가 옮겨갔고,
  ``FlatFileItemReader``가 `InitializingBean` 구현을 걷어내면서 `afterPropertiesSet()` 호출도 빠졌다.
- `MemoJobConfig`: 잡과 스텝을 구성한다.
  4는 `JobBuilderFactory`·``StepBuilderFactory``, 5는 `new StepBuilder(...)`에 `chunk(int, tx)`,
  6은 `chunk(int)` 뒤에 `transactionManager(tx)` 체이닝이다.
- `MemoDbWriterTest`: `ItemWriter` 단위 테스트.
  4는 `write(List)`, 5부터는 ``write(Chunk)``다.
- `MemoJobTest`: 잡 통합 테스트.
  4와 5는 `JobLauncherTestUtils.launchJob(...)`, 6은 ``@SpringBatchTest``로 주입받은
  `JobOperatorTestUtils.startJob(...)`이다.
- `BatchApplication`: 4에만 ``@EnableBatchProcessing``이 붙는다.
  이 애너테이션이 없으면 부트 2.7에서는 `JobBuilderFactory` 빈이 등록되지 않는다.
- `DbConfig`: 메타 DB와 업무 DB를 나눠 쓴다.
  ``@BatchDataSource``의 패키지가 부트 4에서 `org.springframework.boot.batch.jdbc.autoconfigure`로 옮겨갔다.

## 메타 DB

`db/job-repo`에 메타 DB, `db/memo-repo`에 `memo` 테이블이 생긴다.
H2 콘솔로 들여다보려면 `./gradlew jobDb` 또는 `./gradlew mainDb`를 실행한다.

## 업그레이드 작업 지시서

부록 B의 'AI 에이전트와 함께 업그레이드하기' 절에서 안내하는 문서다.
AI 에이전트에 컨텍스트로 바로 넘길 수 있게 작업 원칙, 검색 패턴, 체크리스트, 검수 포인트를 담았다.

- [spring-batch-4-to-6.md](spring-batch-4-to-6.md): 스프링 배치 4 → 6 (스프링 부트 2.7 → 4)
- [spring-batch-5-to-6.md](spring-batch-5-to-6.md): 스프링 배치 5 → 6 (스프링 부트 3 → 4)
