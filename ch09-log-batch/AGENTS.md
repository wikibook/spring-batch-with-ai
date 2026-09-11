# 배치 프로젝트 개발 지침

이 파일의 규칙은 모든 코드 작성과 수정에 적용된다.
규칙과 다르게 구현할 이유가 있으면 진행하기 전에 사람에게 확인한다.

## Java

- `var`는 한 줄만 봐도 타입이 분명할 때만 쓴다.

## Spring

- 의존하는 객체는 생성자 주입으로 받는다. 필드 주입(`@Autowired` 필드)이나 세터 주입은 쓰지 않는다. 테스트 클래스는 예외로 둔다.
- 파일 시스템·시각·원격 호출처럼 테스트에서 통제하기 어려운 대상은 인터페이스로 추상화하고 생성자로 주입받는다.
- 테스트나 실행 프로파일별로 대체할 객체가 아니라면 Bean 등록은 최소화한다.

## Test

- JUnit 6와 AssertJ로 작성한다.
- 프로젝트에서 직접 정의한 인터페이스에 대한 mocking이 필요한 경우, 목(mock) 라이브러리 대신 인터페이스를 구현한 가짜 객체를 만들어 검증한다.
- 정상 경로만 확인하지 말고, 예외가 발생해야 하는 경우와 경계값도 함께 검증한다.
- 테스트 메서드의 의도는 `@DisplayName`으로 표기한다.

### 스프링 부트 관련 테스트

- `@SpringBootTest`로 전체 ApplicationContext를 올리는 것보다는 최소한의 컨텍스트에 의존하는 테스트를 선호한다.
- 가능하면 애플리케이션 컨텍스트를 올리지 않고 객체를 직접 생성해 단위 테스트한다.

### 스프링 배치 관련 테스트

- 애플리케이션 컨텍스트를 로딩하는 테스트에서는 `spring.batch.job.enabled=false`로 잡 자동 실행을 막는다.
- 테스트에서 잡 파라미터를 직접 지정할 때는 `JobOperatorTestUtils.getUniqueJobParametersBuilder()`로 시작해서 파라미터 조합이 중복되지 않게 한다.
- SpEL로 값을 주입받는 스텝에는 `JobOperatorTestUtils.startStep()`으로 실제 실행하는 테스트를 함께 작성한다. 다만 실행 시간이 10초를 넘는 테스트는 한 번만 실행해서 확인하고 `@Disabled`를 붙여 반복 실행에서 제외한다.

### 파일 입출력 테스트

- ItemReader와 ItemWriter를 생성하는 메서드는 애플리케이션 컨텍스트를 올리지 않는 테스트로 먼저 검증한다. 이때 `InitializingBean`을 구현한 리더·라이터라면 `afterPropertiesSet()`을 직접 호출한다.
- 읽기 테스트의 샘플 파일에 정상 형식의 줄만 넣지 않는다. 형식이 잘못된 줄을 넣은 파일로 실패 동작도 검증한다.

### DB 입출력 테스트

- DB 연결 테스트도 같은 ApplicationContext 구성을 재사용해서 ApplicationContext 캐싱을 활용한다.
- 잡의 종단간 테스트에 `@Transactional`을 붙이지 않는다. `spring.batch.jdbc.validate-transaction-state` 속성은 기본값(true)에서 수정하지 않는다.
- 잡의 종단간 테스트가 아니라면 소량의 데이터를 변경하는 테스트는 `@Transactional`을 붙여서 테스트 종료 후 롤백되게 한다.

## 배치 처리 구조

### 대용량 데이터 처리

- 파일이나 DB에서 읽은 전체 데이터를 `java.util.List` 등으로 메모리에 모으지 않는다. 최대 크기가 고정된 운반 단위인 청크를 저장하기 위해서는 `List`를 사용할 수 있다.
- 파일과 DB에 데이터를 쓸 때도 청크 단위로 묶어서 실행한다.
- DB 쓰기는 `NamedParameterJdbcTemplate.batchUpdate()`를 활용한다.

### 잡 구성

- 잡마다 별도의 `@Configuration` 클래스를 만들고, 잡 이름은 그 클래스에 상수로 선언한다.

### 잡 파라미터와 메타데이터

- 실행할 때마다 달라지는 값만 잡 파라미터로 전달한다. 환경별로만 달라지는 값은 application.properties 등 스프링의 속성 관리 체계로 참조한다.
- `JobInstanceAlreadyCompleteException`이 나면 파라미터를 바꾸거나 메타데이터를 지워서 우회하지 말고 사람에게 보고한다. 재실행 허용 여부는 사람이 결정한다.
- 메타데이터 테이블의 데이터를 임의로 DELETE, UPDATE 하지 않는다.
- 잡 파라미터는 `@StepScope`를 붙인 빈에 `@Value`와 SpEL 선언으로 주입받는 방식을 기본으로 한다. 스텝 실행 밖에서도 쓰이는 빈(`Step`을 반환하는 빈, 잡 리스너, 여러 스텝이 공유하는 객체)에는 `@JobScope`를 쓴다.
- 모든 잡에 `DefaultJobParametersValidator`를 지정한다. 잡 파라미터를 추가하면 필수 키 목록도 함께 갱신한다.
- 잡 파라미터용 `Converter`는 클래스나 익명 클래스로 구현한다. 람다 표현식으로 바꾸지 않는다.
- ExecutionContext에는 건수나 시각처럼 직렬화할 수 있는 작은 값만 담는다. 처리 대상 데이터 자체를 담지 않는다.

### 청크 지향 스텝

- 반복해서 읽고 가공하고 쓰는 작업은 Tasklet 하나로 구현하지 말고, ItemReader, ItemProcessor, ItemWriter를 나눈 청크 지향 스텝으로 만든다.
- 직접 구현하기 전에 스프링 배치가 제공하는 ItemReader, ItemProcessor, ItemWriter 구현체가 있는지 먼저 확인하고, 있으면 그것을 쓴다.
- 타입 변환, 필터링, 검증은 ItemReader나 ItemWriter에 섞지 말고 ItemProcessor에 두고, 그 클래스만 따로 테스트한다.
- DB에 쓰는 스텝을 `StepBuilder`로 구성할 때는 `transactionManager(...)`로 `JdbcTransactionManager` 같은 실제 트랜잭션 관리자를 지정한다.
- ItemReader, ItemProcessor, ItemWriter를 반환하면서 `@JobScope` / `@StepScope`가 붙은 `@Bean` 메서드의 반환형은 인터페이스가 아니라 구체적인 클래스로 선언한다. Step을 반환하는 메서드는 예외다.
- 청크 크기는 임의로 정하지 말고, 지시에 없으면 사람에게 묻는다.

### 파일 입출력

- 다수 건의 데이터를 파일로 읽고 쓰는 요구사항에는 스프링 배치가 제공하는 구현체(`FlatFileItemReader`, `JsonItemReader`, `StaxEventItemReader`와 대응하는 ItemWriter)를 먼저 검토한다.
- 파일의 인코딩, 구분자가 지정되지 않았다면 사람에게 묻는다.
- 여러 파일을 다룰 때는 파일 하나를 처리하는 리더·라이터를 먼저 만들고 `MultiResourceItemReader`, `MultiResourceItemWriter`와 조합한다.
