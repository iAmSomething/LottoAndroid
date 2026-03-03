#!/usr/bin/env bash
set -euo pipefail

EMULATOR_REPORT=""
DEVICE_REPORT=""
SAVE_REPORT=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/evaluate-performance-gate.sh [options]

Options:
  --emulator-report <path>  emulator profile report path.
  --device-report <path>    device profile report path.
  --save-report <path>      save decision report markdown path.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --emulator-report)
      EMULATOR_REPORT="${2:-}"
      shift 2
      ;;
    --device-report)
      DEVICE_REPORT="${2:-}"
      shift 2
      ;;
    --save-report)
      SAVE_REPORT="${2:-}"
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

extract_line_value() {
  local file="$1"
  local key="$2"
  grep -E "^- ${key}:" "$file" | tail -n 1 | sed -E "s/^- ${key}:[[:space:]]*//"
}

extract_verdict() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    echo "MISSING"
    return
  fi

  local verdict
  verdict="$(extract_line_value "$file" "Final Verdict" || true)"
  if [[ -z "$verdict" ]]; then
    echo "MISSING"
  else
    echo "$verdict"
  fi
}

extract_device_class() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    echo "unknown"
    return
  fi
  local value
  value="$(extract_line_value "$file" "Device Class" || true)"
  if [[ -z "$value" ]]; then
    echo "unknown"
  else
    echo "$value"
  fi
}

extract_release_blocking() {
  local file="$1"
  if [[ ! -f "$file" ]]; then
    echo "UNKNOWN"
    return
  fi

  local value
  value="$(extract_line_value "$file" "Release Blocking" || true)"
  if [[ -z "$value" ]]; then
    echo "UNKNOWN"
  else
    echo "$value"
  fi
}

EMULATOR_VERDICT="MISSING"
DEVICE_VERDICT="MISSING"
EMULATOR_BLOCKING="UNKNOWN"
DEVICE_BLOCKING="UNKNOWN"
EMULATOR_DEVICE_CLASS="unknown"
DEVICE_DEVICE_CLASS="unknown"

if [[ -n "$EMULATOR_REPORT" && -f "$EMULATOR_REPORT" ]]; then
  EMULATOR_DEVICE_CLASS="$(extract_device_class "$EMULATOR_REPORT")"
  EMULATOR_VERDICT="$(extract_verdict "$EMULATOR_REPORT")"
  EMULATOR_BLOCKING="$(extract_release_blocking "$EMULATOR_REPORT")"
fi

if [[ -n "$DEVICE_REPORT" && -f "$DEVICE_REPORT" ]]; then
  DEVICE_DEVICE_CLASS="$(extract_device_class "$DEVICE_REPORT")"
  DEVICE_VERDICT="$(extract_verdict "$DEVICE_REPORT")"
  DEVICE_BLOCKING="$(extract_release_blocking "$DEVICE_REPORT")"
fi

if [[ "$DEVICE_DEVICE_CLASS" == "emulator" ]]; then
  DEVICE_VERDICT="MISSING"
  DEVICE_BLOCKING="UNKNOWN"
fi

DECISION="PENDING_DEVICE_VALIDATION"
RELEASE_STATUS="PENDING"
FOLLOW_UP="실기기(device) 프로파일 성능 리포트 생성 필요"

if [[ "$DEVICE_VERDICT" == "FAIL" || "$DEVICE_BLOCKING" == "YES" ]]; then
  DECISION="HOLD"
  RELEASE_STATUS="BLOCKED"
  FOLLOW_UP="S07-5 규칙 적용: device FAIL 원인 분석 후 최적화/롤백 결정 필요"
elif [[ "$DEVICE_VERDICT" == "PASS" ]]; then
  if [[ "$EMULATOR_VERDICT" == "FAIL" || "$EMULATOR_VERDICT" == "WARN" ]]; then
    DECISION="PROCEED_WITH_OPTIMIZATION_BACKLOG"
    RELEASE_STATUS="READY_WITH_WARNINGS"
    FOLLOW_UP="S09-4 연동: emulator WARN/FAIL 원인을 다음 루틴 TODO(S06/S08)로 등록"
  else
    DECISION="PROCEED"
    RELEASE_STATUS="READY"
    FOLLOW_UP="성능 게이트 기준 충족"
  fi
fi

if [[ -n "$SAVE_REPORT" ]]; then
  mkdir -p "$(dirname "$SAVE_REPORT")"
  {
    echo "# 성능 릴리즈 판정 리포트"
    echo
    echo "- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')"
    echo "- Emulator report: ${EMULATOR_REPORT:-N/A}"
    echo "- Emulator device class: ${EMULATOR_DEVICE_CLASS}"
    echo "- Emulator verdict: ${EMULATOR_VERDICT}"
    echo "- Device report: ${DEVICE_REPORT:-N/A}"
    echo "- Device class: ${DEVICE_DEVICE_CLASS}"
    echo "- Device verdict: ${DEVICE_VERDICT}"
    echo "- Decision: ${DECISION}"
    echo "- Release Status: ${RELEASE_STATUS}"
    echo "- Follow-up: ${FOLLOW_UP}"
    echo
    echo "## Rule"
    echo "- device FAIL -> HOLD"
    echo "- device PASS + emulator WARN/FAIL -> PROCEED_WITH_OPTIMIZATION_BACKLOG"
    echo "- device PASS + emulator PASS -> PROCEED"
    echo "- device missing -> PENDING_DEVICE_VALIDATION"
  } > "$SAVE_REPORT"
  echo "[INFO] Saved decision report: $SAVE_REPORT"
fi

echo "[INFO] Decision=${DECISION}, ReleaseStatus=${RELEASE_STATUS}"

if [[ "$DECISION" == "HOLD" ]]; then
  echo "[FAIL] Performance release decision is HOLD."
  exit 1
fi

if [[ "$DECISION" == "PENDING_DEVICE_VALIDATION" ]]; then
  echo "[WARN] Device report missing. Release decision is pending."
fi

exit 0
