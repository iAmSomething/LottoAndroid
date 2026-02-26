#!/usr/bin/env bash
set -euo pipefail

SERIAL=""
SAVE_LOG_FILE=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-ops-observability-check.sh [--serial <adb-serial>] [--save-log <path>]

Options:
  --serial    adb device serial to target.
  --save-log  Persist collected analytics log to a file.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --save-log)
      SAVE_LOG_FILE="${2:-}"
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

TEST_CLASSES="com.weeklylotto.app.MainNavigationInstrumentedTest,com.weeklylotto.app.WeeklySaveFlowInstrumentedTest"

echo "[INFO] Clear previous analytics logcat buffer ($SERIAL)"
adb -s "$SERIAL" logcat -c

echo "[INFO] Run observability instrumentation tests"
ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class="$TEST_CLASSES"

TMP_LOG_FILE="$(mktemp)"
adb -s "$SERIAL" logcat -d -s WeeklyLottoAnalytics:I > "$TMP_LOG_FILE"

if [[ -n "$SAVE_LOG_FILE" ]]; then
  mkdir -p "$(dirname "$SAVE_LOG_FILE")"
  cp "$TMP_LOG_FILE" "$SAVE_LOG_FILE"
  echo "[INFO] Saved analytics log: $SAVE_LOG_FILE"
fi

echo "[INFO] Verify ops observability samples"
./scripts/verify-analytics-events.sh --profile ops-core --log-file "$TMP_LOG_FILE"

rm -f "$TMP_LOG_FILE"
echo "[PASS] Ops observability check completed."
