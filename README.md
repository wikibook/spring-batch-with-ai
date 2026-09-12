# AI 에이전트와 함께하는 스프링 배치

『AI 에이전트와 함께하는 스프링 배치』(위키북스)의 예제 코드 저장소

## 디렉터리 구성

- 2장 'AI 에이전트와 함께하는 개발 환경 준비': [`ch02-disk-check`](ch02-disk-check/)
- 3장 '스프링 부트를 이용한 CLI 애플리케이션': [`ch03-log-batch`](ch03-log-batch/)
- 4장 '대용량을 의식한 파일과 DB 처리': [`ch04-log-batch`](ch04-log-batch/)
- 5장 '잡, 스텝, 태스클릿': [`ch05-log-batch`](ch05-log-batch/)
- 6장 '잡 저장소와 잡 파라미터': [`ch06-log-batch`](ch06-log-batch/)
- 7장 '잡 파라미터 심화 활용과 ExecutionContext': [`ch07-log-batch`](ch07-log-batch/)
- 8장 'ItemReader, ItemProcessor, ItemWriter': [`ch08-log-batch`](ch08-log-batch/)
- 9장 '파일 읽기와 쓰기': [`ch09-log-batch`](ch09-log-batch/)
- 10장 '스프링 JDBC로 DB 읽기와 쓰기': [`ch10-log-batch`](ch10-log-batch/)
- 11장 '잡 재시작과 실패한 아이템 처리': [`ch11-health-checker`](ch11-health-checker/)
- 12장 '이벤트 리스너': [`ch12-health-checker`](ch12-health-checker/)
- 13장 '스텝의 흐름 제어하기': [`ch13-health-checker`](ch13-health-checker/)
- 14장 '배치 모듈의 의존 관계와 실행 방식': [`ch14-modules`](ch14-modules/)
- 15장 '잡 실행 모니터링': 14장의 [`ch14-modules`](ch14-modules/)를 이어서 쓴다.
- 부록 A '스프링 프레임워크의 재시도 기능': [`ap1-spring-retry`](ap1-spring-retry/)
- 부록 B '스프링 배치 4, 5에서 6으로 업그레이드하기': [`ap2-batch-upgrade`](ap2-batch-upgrade/)
    - 버전별 예제
        - 스프링 배치 4 예제 : [`memo-batch4`](ap2-batch-upgrade/memo-batch4/)
        - 스프링 배치 5 예제 : [`memo-batch5`](ap2-batch-upgrade/memo-batch5/)
        - 스프링 배치 6 예제 : [`memo-batch6`](ap2-batch-upgrade/memo-batch6/)
    - AI 에이전트에 넘길 업그레이드 작업 지시서
        - [`spring-batch-4-to-6.md`](ap2-batch-upgrade/spring-batch-4-to-6.md)
        - [`spring-batch-5-to-6.md`](ap2-batch-upgrade/spring-batch-5-to-6.md)

## 실행 환경

- JDK 25 
- 스프링 부트 4.1.x, 스프링 배치 6.0.x

[`ap2-batch-upgrade`](ap2-batch-upgrade/)의 스프링 배치 4,5 예제는 예외. `ap2-batch-upgrade/memo-batch4`는 JDK 17


## 빌드 방법

- 각 디렉터리는 독립된 그레이들 프로젝트다. 해당 디렉터리로 이동해서 `./gradlew test`로 테스트를 실행한다.
- [`ch14-modules`](ch14-modules/)는 멀티모듈 프로젝트라 루트에서 `./gradlew build`로 전체를 빌드한다.
- [`ap2-batch-upgrade`](ap2-batch-upgrade/)의 세 프로젝트는 각각 독립 빌드이며 자세한 실행 방법은 그 디렉터리의 `README.md`에 있다.

