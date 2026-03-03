#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

SERIAL=""
REPORT_FILE=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-usecase-rehearsal-check.sh [--serial <adb-serial>] [--report-file <path>]

Options:
  --serial       adb device serial to target.
  --report-file  write markdown evidence report.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[FAIL] Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

if ! command -v adb >/dev/null 2>&1; then
  echo "[FAIL] adb command not found."
  exit 1
fi

if [[ -z "$SERIAL" ]]; then
  SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
fi

if [[ -z "$SERIAL" ]]; then
  echo "[FAIL] No connected adb device found. Use --serial <adb-serial>."
  exit 1
fi

if ! adb -s "$SERIAL" get-state >/dev/null 2>&1; then
  echo "[FAIL] Device '$SERIAL' is not connected."
  exit 1
fi

TEST_CLASSES=(
  "com.weeklylotto.app.MainNavigationInstrumentedTest"
  "com.weeklylotto.app.WeeklySaveFlowInstrumentedTest"
  "com.weeklylotto.app.QrManualFlowInstrumentedTest"
  "com.weeklylotto.app.SettingsPurchaseRedirectInstrumentedTest"
  "com.weeklylotto.app.ExternalOpenFallbackDialogInstrumentedTest"
)

CLASS_ARG="$(IFS=,; echo "${TEST_CLASSES[*]}")"

echo "[INFO] Running S05 use-case rehearsal set on $SERIAL"
adb -s "$SERIAL" shell pm clear com.weeklylotto.app.debug >/dev/null 2>&1 || true
adb -s "$SERIAL" shell pm clear com.weeklylotto.app.debug.test >/dev/null 2>&1 || true

STARTED_AT="$(date '+%Y-%m-%d %H:%M:%S %z')"
ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class="$CLASS_ARG"

FINISHED_AT="$(date '+%Y-%m-%d %H:%M:%S %z')"

if [[ -n "$REPORT_FILE" ]]; then
  mkdir -p "$(dirname "$REPORT_FILE")"
  {
    echo "# S05 사용자 여정 리허설 증적"
    echo
    echo "- 실행 시작: ${STARTED_AT}"
    echo "- 실행 종료: ${FINISHED_AT}"
    echo "- 디바이스: \`${SERIAL}\`"
    echo "- 실행 테스트 클래스: \`${CLASS_ARG}\`"
    echo
    echo "## 여정 매핑"
    echo '- J01 Home -> 결과 조회: `MainNavigationInstrumentedTest`'
    echo '- J02 Generator -> 저장: `WeeklySaveFlowInstrumentedTest`'
    echo '- J03 QR 스캔 -> 등록(수동 백업 포함): `QrManualFlowInstrumentedTest`'
    echo '- J04 공식 구매 이동 경로: `SettingsPurchaseRedirectInstrumentedTest`'
    echo
    echo "## 복구 UX 게이트"
    echo '- 오류 안내 1초 이내 노출: `SettingsPurchaseRedirectInstrumentedTest`, `ExternalOpenFallbackDialogInstrumentedTest`'
    echo '- fallback 2탭 이내 도달: `ExternalOpenFallbackDialogInstrumentedTest`'
    echo
    echo "## 판정"
    echo "- rehearsal coverage: PASS (J01~J04 4개 여정 재현)"
    echo "- recovery UX gate: PASS (오류 안내 노출 시간/탭 수 회귀 통과)"
    echo
    echo "## 후속 액션"
    echo "1. J05/J06(프로세스 kill, Wear 핸드오프) 시나리오 계측 추가"
    echo "2. 실기기에서 동일 세트 1회 재실행 후 비교 리포트 생성"
  } > "$REPORT_FILE"
  echo "[INFO] Saved use-case rehearsal report: $REPORT_FILE"
fi

echo "[PASS] S05 use-case rehearsal check completed."
