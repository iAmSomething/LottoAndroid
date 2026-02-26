#!/usr/bin/env bash
set -euo pipefail

TAG="WeeklyLottoAnalytics"
SERIAL=""
LOG_FILE=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/verify-analytics-events.sh [--serial <adb-serial>] [--log-file <path>]

Options:
  --serial    adb device serial to target.
  --log-file  validate from a saved log file instead of adb logcat.

Notes:
  - Without --log-file, this script reads `adb logcat -d -s WeeklyLottoAnalytics:I`.
  - For reliable results, run target scenarios first, then execute this script.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --log-file)
      LOG_FILE="${2:-}"
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

required_events=(
  "motion_splash_shown"
  "motion_splash_skip"
  "interaction_cta_press"
  "interaction_ball_lock_toggle"
  "interaction_sheet_apply"
)

load_lines_from_adb() {
  local -a adb_cmd=("adb")
  if [[ -n "$SERIAL" ]]; then
    adb_cmd+=("-s" "$SERIAL")
  fi
  if ! "${adb_cmd[@]}" get-state >/dev/null 2>&1; then
    echo "[FAIL] No adb device connected. Provide --log-file or connect a device."
    exit 1
  fi
  "${adb_cmd[@]}" logcat -d -s "${TAG}:I"
}

if [[ -n "$LOG_FILE" ]]; then
  if [[ ! -f "$LOG_FILE" ]]; then
    echo "[FAIL] Log file not found: $LOG_FILE"
    exit 1
  fi
  lines="$(cat "$LOG_FILE")"
else
  lines="$(load_lines_from_adb)"
fi

if [[ -z "${lines// }" ]]; then
  echo "[FAIL] No analytics logs found."
  exit 1
fi

missing=0
schema_error=0

echo "[INFO] Analytics sample summary"
for event in "${required_events[@]}"; do
  count="$(printf "%s\n" "$lines" | rg -c "$event" || true)"
  count="${count:-0}"
  if [[ "$count" -eq 0 ]]; then
    echo "  - $event: 0 (missing)"
    missing=$((missing + 1))
  else
    echo "  - $event: $count"
  fi
done

interaction_events=(
  "interaction_cta_press"
  "interaction_ball_lock_toggle"
  "interaction_sheet_apply"
)

for event in "${interaction_events[@]}"; do
  valid_count="$(printf "%s\n" "$lines" | rg -c "${event} \\| .*screen=.*component=.*action=" || true)"
  valid_count="${valid_count:-0}"
  if [[ "$valid_count" -eq 0 ]]; then
    echo "[WARN] $event schema sample not found (screen/component/action)."
    schema_error=$((schema_error + 1))
  fi
done

if [[ "$missing" -gt 0 ]]; then
  echo "[FAIL] Missing required analytics events: $missing"
  exit 1
fi

if [[ "$schema_error" -gt 0 ]]; then
  echo "[FAIL] Interaction event schema samples missing: $schema_error"
  exit 1
fi

echo "[PASS] Required analytics events and schema samples verified."
