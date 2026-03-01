#!/usr/bin/env bash
set -euo pipefail

TAG="AndroidRuntime"
SERIAL=""
LOG_FILE=""
REPORT_FILE=""
MAX_EVENTS="200"

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/classify-crash-anr-template.sh \
    [--serial <adb-serial> | --log-file <path>] \
    [--report-file <path>] \
    [--max-events <count>]

Notes:
  - without --log-file, this script reads `adb logcat -d`
  - input can be logcat, Crashlytics export text, or issue summary lines
  - output is a markdown template report for triage handoff
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
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --max-events)
      MAX_EVENTS="${2:-}"
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

if [[ ! "$MAX_EVENTS" =~ ^[0-9]+$ ]]; then
  echo "[FAIL] --max-events must be an integer. got: $MAX_EVENTS"
  exit 1
fi

load_lines_from_adb() {
  local -a adb_cmd=("adb")
  if [[ -n "$SERIAL" ]]; then
    adb_cmd+=("-s" "$SERIAL")
  fi

  if ! "${adb_cmd[@]}" get-state >/dev/null 2>&1; then
    echo "[FAIL] No adb device connected. Provide --log-file or connect a device."
    exit 1
  fi

  "${adb_cmd[@]}" logcat -d
}

if [[ -n "$LOG_FILE" ]]; then
  if [[ ! -f "$LOG_FILE" ]]; then
    echo "[FAIL] Log file not found: $LOG_FILE"
    exit 1
  fi
  lines="$(cat "$LOG_FILE")"
  source_desc="log_file:$LOG_FILE"
else
  lines="$(load_lines_from_adb)"
  source_desc="adb:${SERIAL:-auto}"
fi

if [[ -z "${lines// }" ]]; then
  echo "[FAIL] No input lines found."
  exit 1
fi

is_candidate_line() {
  local line="$1"
  local lower
  lower="$(printf "%s" "$line" | tr '[:upper:]' '[:lower:]')"

  [[ "$lower" == *"anr"* ]] ||
  [[ "$lower" == *"input dispatching timed out"* ]] ||
  [[ "$lower" == *"did not respond"* ]] ||
  [[ "$lower" == *"fatal exception"* ]] ||
  [[ "$lower" == *"exception"* ]] ||
  [[ "$lower" == *"sigabrt"* ]] ||
  [[ "$lower" == *"error"* ]]
}

classify_event() {
  local line="$1"
  local lower
  lower="$(printf "%s" "$line" | tr '[:upper:]' '[:lower:]')"

  if [[ "$lower" == *"anr"* ]] || [[ "$lower" == *"input dispatching timed out"* ]] || [[ "$lower" == *"did not respond"* ]]; then
    if [[ "$lower" == *"startup"* ]] || [[ "$lower" == *"application.oncreate"* ]] || [[ "$lower" == *"appgraph.init"* ]]; then
      echo "anr_startup"
      return
    fi
    if [[ "$lower" == *"input dispatching timed out"* ]] || [[ "$lower" == *"main thread"* ]] || [[ "$lower" == *"choreographer"* ]]; then
      echo "anr_main_thread_block"
      return
    fi
    echo "anr_other"
    return
  fi

  if [[ "$lower" == *"outofmemoryerror"* ]] || [[ "$lower" == *"no space left on device"* ]]; then
    echo "crash_resource_pressure"
    return
  fi

  if [[ "$lower" == *"sockettimeoutexception"* ]] || [[ "$lower" == *"unknownhostexception"* ]] || [[ "$lower" == *"sslhandshakeexception"* ]] || [[ "$lower" == *"connectexception"* ]]; then
    echo "crash_network"
    return
  fi

  if [[ "$lower" == *"sqlite"* ]] || [[ "$lower" == *"room"* ]] || [[ "$lower" == *"disk i/o"* ]] || [[ "$lower" == *"cursorwindow"* ]]; then
    echo "crash_storage"
    return
  fi

  if [[ "$lower" == *"camera"* ]] || [[ "$lower" == *"camerax"* ]] || [[ "$lower" == *"mlkit"* ]] || [[ "$lower" == *"imageanalysis"* ]]; then
    echo "crash_camera"
    return
  fi

  if [[ "$lower" == *"activitynotfoundexception"* ]] || [[ "$lower" == *"unable to resolve activity"* ]] || [[ "$lower" == *"customtabs"* ]]; then
    echo "crash_external_redirect"
    return
  fi

  if [[ "$lower" == *"illegalstateexception"* ]] || [[ "$lower" == *"nullpointerexception"* ]] || [[ "$lower" == *"kotlinnullpointerexception"* ]] || [[ "$lower" == *"fatal exception"* ]] || [[ "$lower" == *"sigabrt"* ]]; then
    echo "crash_runtime"
    return
  fi

  echo "crash_other"
}

category_label() {
  case "$1" in
    anr_startup) echo "ANR / Startup" ;;
    anr_main_thread_block) echo "ANR / Main Thread Block" ;;
    anr_other) echo "ANR / Other" ;;
    crash_resource_pressure) echo "Crash / Resource Pressure" ;;
    crash_network) echo "Crash / Network" ;;
    crash_storage) echo "Crash / Storage" ;;
    crash_camera) echo "Crash / Camera" ;;
    crash_external_redirect) echo "Crash / External Redirect" ;;
    crash_runtime) echo "Crash / Runtime" ;;
    *) echo "Crash / Other" ;;
  esac
}

category_severity() {
  case "$1" in
    anr_startup|anr_main_thread_block|crash_runtime|crash_resource_pressure)
      echo "P0"
      ;;
    anr_other|crash_storage|crash_camera)
      echo "P1"
      ;;
    crash_network|crash_external_redirect)
      echo "P2"
      ;;
    *)
      echo "P3"
      ;;
  esac
}

category_owner() {
  case "$1" in
    anr_startup|anr_main_thread_block|anr_other)
      echo "app-platform"
      ;;
    crash_runtime)
      echo "feature-owner"
      ;;
    crash_resource_pressure)
      echo "app-platform"
      ;;
    crash_storage)
      echo "data-owner"
      ;;
    crash_camera)
      echo "qr-owner"
      ;;
    crash_network)
      echo "api-owner"
      ;;
    crash_external_redirect)
      echo "navigation-owner"
      ;;
    *)
      echo "triage-rotation"
      ;;
  esac
}

category_action() {
  case "$1" in
    anr_startup)
      echo "App start path profile + cold start trace 확인"
      ;;
    anr_main_thread_block)
      echo "메인스레드 long task 추적 + blocking call 제거"
      ;;
    anr_other)
      echo "ANR trace 수집 후 block 지점 분해"
      ;;
    crash_resource_pressure)
      echo "OOM/disk pressure 재현 + 객체/파일 점유량 점검"
      ;;
    crash_network)
      echo "timeout/offline fallback 경로 및 retry 정책 점검"
      ;;
    crash_storage)
      echo "DB/파일 I/O 예외 복구 경로 점검"
      ;;
    crash_camera)
      echo "camera lifecycle/permission race 재현 점검"
      ;;
    crash_external_redirect)
      echo "intent 해상도/fallback(copy/open browser) 점검"
      ;;
    crash_runtime)
      echo "stacktrace root-cause + null/state guard 추가"
      ;;
    *)
      echo "원인 미분류: 원본 stacktrace 확보 후 재분류"
      ;;
  esac
}

sanitize_cell() {
  printf "%s" "$1" | sed -e 's/|/\\|/g' -e 's/`/\x27/g'
}

classified_file="$(mktemp)"
summary_file="$(mktemp)"
trap 'rm -f "$classified_file" "$summary_file"' EXIT

candidate_count=0
selected_count=0

while IFS= read -r line; do
  if [[ -z "${line// }" ]]; then
    continue
  fi
  if ! is_candidate_line "$line"; then
    continue
  fi

  candidate_count=$((candidate_count + 1))
  if [[ "$selected_count" -ge "$MAX_EVENTS" ]]; then
    continue
  fi

  category="$(classify_event "$line")"
  printf "%s\t%s\n" "$category" "$line" >> "$classified_file"
  selected_count=$((selected_count + 1))
done <<< "$lines"

if [[ "$selected_count" -eq 0 ]]; then
  echo "[FAIL] No crash/ANR candidate lines found in input."
  exit 1
fi

awk -F '\t' '
{
  count[$1]++
  if (!($1 in sample)) {
    sample[$1] = $2
  }
}
END {
  for (key in count) {
    printf "%s\t%d\t%s\n", key, count[key], sample[key]
  }
}
' "$classified_file" | sort -t$'\t' -k2,2nr > "$summary_file"

if [[ -z "$REPORT_FILE" ]]; then
  date_suffix="$(date '+%Y-%m-%d')"
  REPORT_FILE="docs/assets/distribution/crash_anr_classification_local_${date_suffix}.md"
fi

mkdir -p "$(dirname "$REPORT_FILE")"
timestamp_utc="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"

{
  echo "# Crash/ANR Auto Classification Report"
  echo
  echo "- Generated at (UTC): ${timestamp_utc}"
  echo "- Source: ${source_desc}"
  echo "- Candidate lines detected: ${candidate_count}"
  echo "- Classified lines used: ${selected_count} (max ${MAX_EVENTS})"
  echo
  echo "## Summary"
  echo
  echo "| Category | Count | Severity | Owner | Immediate Action |"
  echo "|---|---:|---|---|---|"

  while IFS=$'\t' read -r category count sample; do
    label="$(category_label "$category")"
    severity="$(category_severity "$category")"
    owner="$(category_owner "$category")"
    action="$(category_action "$category")"
    echo "| $(sanitize_cell "$label") | ${count} | ${severity} | ${owner} | $(sanitize_cell "$action") |"
  done < "$summary_file"

  echo
  echo "## Representative Signals"
  echo
  while IFS=$'\t' read -r category count sample; do
    label="$(category_label "$category")"
    trimmed="$(printf "%s" "$sample" | cut -c1-220)"
    echo "- **${label}** (${count})"
    echo "  - sample: \`$(sanitize_cell "$trimmed")\`"
  done < "$summary_file"

  echo
  echo "## Triage Template"
  echo
  echo "- 재현 여부: [ ] 재현됨 / [ ] 미재현"
  echo "- 영향 범위: [ ] startup [ ] result [ ] generator [ ] manage [ ] settings [ ] wear"
  echo "- 임시 완화책: [ ] feature flag [ ] retry [ ] fallback UX [ ] 안내문구"
  echo "- 다음 액션: owner 할당 + ETA + 회귀 테스트 항목 정의"
} > "$REPORT_FILE"

echo "[PASS] Crash/ANR classification report generated: $REPORT_FILE"
