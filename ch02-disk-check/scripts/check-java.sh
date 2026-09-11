#!/bin/bash
# 에이전트가 파일을 수정할 때마다 실행되는 훅.
# src/ 아래의 자바 파일이 수정됐으면 AGENTS.md의 테스트 규약을 검사하고 곧바로 컴파일해서
# 오류를 에이전트에게 돌려준다.

input=$(cat)

file_path=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -n 1)
if [ -z "$file_path" ]; then
  # 도구에 따라 입력 JSON의 형식이 다르므로 src/ 아래 .java 언급 여부로 보완한다.
  case "$input" in
    *'src/'*'.java'*) file_path="src/unknown.java" ;;
  esac
fi

# src/ 밖의 자바 파일은 컴파일 대상이 아니므로 걸러낸다.
# 무관한 파일을 수정한 턴에 기존 컴파일 오류가 전달되면 에이전트가 엉뚱한 수정을 하러 간다.
case "$file_path" in
  src/*.java|*/src/*.java) ;;
  *) exit 0 ;;
esac

cd "${CLAUDE_PROJECT_DIR:-$(dirname "$0")/..}" || exit 0

# 테스트 클래스라면 모든 @Test 메서드에 @DisplayName이 있는지 검사한다.
case "$file_path" in
  *src/test/*Test.java)
    if [ -f "$file_path" ]; then
      test_count=$(grep -c '@Test' "$file_path")
      name_count=$(grep -c '@DisplayName' "$file_path")
      if [ "$test_count" -gt "$name_count" ]; then
        echo "규약 위반: @Test 메서드는 ${test_count}개인데 @DisplayName은 ${name_count}개다." >&2
        echo "AGENTS.md의 테스트 규약에 따라 모든 테스트 메서드에 @DisplayName으로 의도를 표기하라." >&2
        exit 2
      fi
    fi
    ;;
esac

output=$(./gradlew compileJava compileTestJava 2>&1)
if [ $? -ne 0 ]; then
  echo "컴파일 실패: 아래 오류를 보고 코드를 고쳐라." >&2
  printf '%s\n' "$output" | tail -n 40 >&2
  exit 2
fi
exit 0
