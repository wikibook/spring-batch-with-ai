#!/bin/bash
# 에이전트가 작업을 마치려 할 때 실행되는 훅.
# 전체 테스트를 실행하고, 실패가 있으면 종료 코드 2로 작업 종료를 막는다.

input=$(cat)

# 이 훅이 돌려보낸 작업이 끝날 때 훅이 또 실행되어 무한 반복하지 않게 방어한다.
if printf '%s' "$input" | grep -Eq '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-$(dirname "$0")/..}" || exit 0

output=$(./gradlew test 2>&1)
if [ $? -ne 0 ]; then
  echo "테스트 실패: 아래 출력을 보고 코드를 고친 뒤 작업을 마무리하라." >&2
  printf '%s\n' "$output" | tail -n 40 >&2
  exit 2
fi
exit 0
