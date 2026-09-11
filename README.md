# AI 에이전트와 함께하는 스프링 배치

『AI 에이전트와 함께하는 스프링 배치』(위키북스)의 예제 코드 저장소다.

## 디렉터리 구성

- `ch02-disk-check`부터 `ch14-modules`까지 `ch`로 시작하는 디렉터리가 각 장의 예제 프로젝트다. 디렉터리 번호가 장 번호다.
  15장은 14장의 `ch14-modules`를 이어서 쓴다.
- `ap`로 시작하는 디렉터리는 부록의 예제다.
  - `ap1-spring-retry`: 부록 A '스프링 프레임워크의 재시도 기능'
  - `ap2-batch-upgrade`: 부록 B '스프링 배치 4, 5에서 6으로 업그레이드하기'.
    같은 메모 잡을 스프링 배치 4·5·6으로 각각 구현한 `memo-batch4`·`memo-batch5`·`memo-batch6`과
    AI 에이전트에 넘길 업그레이드 작업 지시서 `spring-batch-4-to-6.md`, `spring-batch-5-to-6.md`가 있다.

각 디렉터리는 독립된 그레이들 프로젝트다. 해당 디렉터리로 이동해서 `./gradlew test`로 테스트를 실행한다.
`ch14-modules`는 멀티모듈 프로젝트라 루트에서 `./gradlew build`로 전체를 빌드한다.
`ap2-batch-upgrade`의 세 프로젝트는 각각 독립 빌드이며 자세한 실행 방법은 그 디렉터리의 `README.md`에 있다.

## 실행 환경

- JDK 25 (`ap2-batch-upgrade/memo-batch4`만 JDK 17)
- 스프링 부트 4, 스프링 배치 6

각 프로젝트의 `AGENTS.md`는 그 장에서 AI 에이전트에게 준 지침 파일이다. `CLAUDE.md`는 `AGENTS.md`를 가리키는 심볼릭 링크다.
