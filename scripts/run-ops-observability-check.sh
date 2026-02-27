#!/usr/bin/env bash
set -euo pipefail

SERIAL=""
SAVE_LOG_FILE=""
THRESHOLD_REPORT_FILE=""
SKIP_THRESHOLD_CHECK=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-ops-observability-check.sh [--serial <adb-serial>] [--save-log <path>] [--threshold-report <path>] [--skip-threshold-check]

Options:
  --serial    adb device serial to target.
  --save-log  Persist collected analytics log to a file.
  --threshold-report  Persist threshold evaluation report to a markdown file.
  --skip-threshold-check  Skip `evaluate-ops-observability-threshold.sh` execution.
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
    --threshold-report)
      THRESHOLD_REPORT_FILE="${2:-}"
      shift 2
      ;;
    --skip-threshold-check)
      SKIP_THRESHOLD_CHECK=1
      shift
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
trap 'rm -f "$TMP_LOG_FILE"' EXIT
adb -s "$SERIAL" logcat -d -s WeeklyLottoAnalytics:I > "$TMP_LOG_FILE"

if [[ -n "$SAVE_LOG_FILE" ]]; then
  mkdir -p "$(dirname "$SAVE_LOG_FILE")"
  cp "$TMP_LOG_FILE" "$SAVE_LOG_FILE"
  echo "[INFO] Saved analytics log: $SAVE_LOG_FILE"
fi

echo "[INFO] Verify ops observability samples"
./scripts/verify-analytics-events.sh --profile ops-core --log-file "$TMP_LOG_FILE"

if [[ "$SKIP_THRESHOLD_CHECK" -eq 0 ]]; then
  echo "[INFO] Evaluate ops observability thresholds"
  threshold_cmd=(
    ./scripts/evaluate-ops-observability-threshold.sh
    --log-file "$TMP_LOG_FILE"
  )
  if [[ -n "$THRESHOLD_REPORT_FILE" ]]; then
    threshold_cmd+=(--report-file "$THRESHOLD_REPORT_FILE")
  fi
  "${threshold_cmd[@]}"
fi

echo "[PASS] Ops observability check completed."
