#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
POLL_INTERVAL=10
TIMEOUT_SECONDS=0
SAVE_BLOCKED_REPORT=0
SKIP_WEAR_INSTALL=1
ORCHESTRATOR_REPORT=""
CHECKPOINT_REPORT=""
BK_DECISION_REPORT=""
WEAR_REPORT=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-all-physical-gates-when-ready.sh [options]

Options:
  --date-tag <yyyy-mm-dd>          report date tag.
  --poll-interval <seconds>        device polling interval (default: 10).
  --timeout-seconds <seconds>      timeout (0 means infinite).
  --save-blocked-report            on timeout, generate blocked reports.
  --no-skip-wear-install           include :wear:installDebug in P-004 run.
  --orchestrator-report <path>     orchestrator summary report path.
  --checkpoint-report <path>       checkpoint report path.
  --bk-decision-report <path>      BK decision report path.
  --wear-report <path>             Wear P-004 report path.
  -h, --help                       show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --poll-interval)
      POLL_INTERVAL="${2:-10}"
      shift 2
      ;;
    --timeout-seconds)
      TIMEOUT_SECONDS="${2:-0}"
      shift 2
      ;;
    --save-blocked-report)
      SAVE_BLOCKED_REPORT=1
      shift 1
      ;;
    --no-skip-wear-install)
      SKIP_WEAR_INSTALL=0
      shift 1
      ;;
    --orchestrator-report)
      ORCHESTRATOR_REPORT="${2:-}"
      shift 2
      ;;
    --checkpoint-report)
      CHECKPOINT_REPORT="${2:-}"
      shift 2
      ;;
    --bk-decision-report)
      BK_DECISION_REPORT="${2:-}"
      shift 2
      ;;
    --wear-report)
      WEAR_REPORT="${2:-}"
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

if ! [[ "$POLL_INTERVAL" =~ ^[0-9]+$ ]] || [[ "$POLL_INTERVAL" -lt 1 ]]; then
  echo "[FAIL] --poll-interval must be a positive integer."
  exit 1
fi
if ! [[ "$TIMEOUT_SECONDS" =~ ^[0-9]+$ ]]; then
  echo "[FAIL] --timeout-seconds must be zero or a positive integer."
  exit 1
fi

if [[ -z "$ORCHESTRATOR_REPORT" ]]; then
  ORCHESTRATOR_REPORT="docs/assets/distribution/physical_gates_orchestrator_${DATE_TAG}.md"
fi
if [[ -z "$CHECKPOINT_REPORT" ]]; then
  CHECKPOINT_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
fi
if [[ -z "$BK_DECISION_REPORT" ]]; then
  BK_DECISION_REPORT="docs/assets/distribution/performance_release_decision_checkpoint_${DATE_TAG}.md"
fi
if [[ -z "$WEAR_REPORT" ]]; then
  WEAR_REPORT="docs/assets/distribution/wear_p4_device_evidence_checkpoint_${DATE_TAG}.md"
fi

mkdir -p "$(dirname "$ORCHESTRATOR_REPORT")"

is_watch_serial() {
  local serial="$1"
  local characteristics
  characteristics="$(adb -s "$serial" shell getprop ro.build.characteristics 2>/dev/null | tr -d '\r' || true)"
  [[ "$characteristics" == *watch* ]]
}

find_phone_serial() {
  local serial
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    if ! is_watch_serial "$serial"; then
      echo "$serial"
      return 0
    fi
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')
  return 1
}

find_wear_pair() {
  local ranked=""
  local serial size area
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    if is_watch_serial "$serial"; then
      size="$(adb -s "$serial" shell wm size 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical size/ {print $2; exit}')"
      area=0
      if [[ "$size" =~ ^([0-9]+)x([0-9]+)$ ]]; then
        area="$(( ${BASH_REMATCH[1]} * ${BASH_REMATCH[2]} ))"
      fi
      ranked+="${area} ${serial}"$'\n'
    fi
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')

  [[ -z "$ranked" ]] && return 1
  local sorted count small large
  sorted="$(printf '%s' "$ranked" | sed '/^$/d' | sort -n)"
  count="$(printf '%s\n' "$sorted" | sed '/^$/d' | wc -l | tr -d ' ')"
  [[ "$count" -lt 2 ]] && return 1
  small="$(printf '%s\n' "$sorted" | awk 'NR==1 {print $2}')"
  large="$(printf '%s\n' "$sorted" | awk 'END {print $2}')"
  [[ -z "$small" || -z "$large" || "$small" == "$large" ]] && return 1
  echo "$small $large"
}

BK_STATUS="BLOCKED"
P4_STATUS="BLOCKED"
BK_SERIAL_USED=""
P4_SMALL_USED=""
P4_LARGE_USED=""

started_at="$(date +%s)"
echo "[INFO] Waiting for physical phone + wear pair..."
echo "       poll_interval=${POLL_INTERVAL}s timeout=${TIMEOUT_SECONDS}s date_tag=${DATE_TAG}"

while true; do
  if [[ "$BK_STATUS" != "PASS" ]]; then
    phone_serial="$(find_phone_serial || true)"
    if [[ -n "${phone_serial:-}" ]]; then
      echo "[INFO] Phone physical detected: $phone_serial"
      set +e
      ./scripts/run-bk-device-gate.sh \
        --serial "$phone_serial" \
        --date-tag "$DATE_TAG" \
        --decision-report "$BK_DECISION_REPORT"
      bk_code=$?
      set -e
      if [[ "$bk_code" -eq 0 ]]; then
        BK_STATUS="PASS"
        BK_SERIAL_USED="$phone_serial"
      else
        BK_STATUS="FAIL"
      fi
    fi
  fi

  if [[ "$P4_STATUS" != "PASS" ]]; then
    wear_pair="$(find_wear_pair || true)"
    if [[ -n "${wear_pair:-}" ]]; then
      p4_small="$(printf '%s' "$wear_pair" | awk '{print $1}')"
      p4_large="$(printf '%s' "$wear_pair" | awk '{print $2}')"
      echo "[INFO] Wear pair detected: small=$p4_small large=$p4_large"
      cmd=(./scripts/run-p4-wear-proof-gate.sh --small-serial "$p4_small" --large-serial "$p4_large" --date-tag "$DATE_TAG" --report-path "$WEAR_REPORT")
      if [[ "$SKIP_WEAR_INSTALL" -eq 1 ]]; then
        cmd+=(--skip-install)
      fi
      set +e
      "${cmd[@]}"
      p4_code=$?
      set -e
      if [[ "$p4_code" -eq 0 ]]; then
        P4_STATUS="PASS"
        P4_SMALL_USED="$p4_small"
        P4_LARGE_USED="$p4_large"
      else
        P4_STATUS="FAIL"
      fi
    fi
  fi

  if [[ "$BK_STATUS" == "PASS" && "$P4_STATUS" == "PASS" ]]; then
    break
  fi

  if [[ "$TIMEOUT_SECONDS" -gt 0 ]]; then
    now="$(date +%s)"
    elapsed=$((now - started_at))
    if [[ "$elapsed" -ge "$TIMEOUT_SECONDS" ]]; then
      echo "[WARN] Timeout reached before all physical gates were ready."
      if [[ "$SAVE_BLOCKED_REPORT" -eq 1 ]]; then
        ./scripts/run-bk-device-gate.sh \
          --date-tag "$DATE_TAG" \
          --decision-report "$BK_DECISION_REPORT" \
          --save-blocked-report || true

        p4_block_cmd=(./scripts/run-p4-wear-proof-gate.sh --date-tag "$DATE_TAG" --report-path "$WEAR_REPORT" --save-blocked-report)
        if [[ "$SKIP_WEAR_INSTALL" -eq 1 ]]; then
          p4_block_cmd+=(--skip-install)
        fi
        "${p4_block_cmd[@]}" || true
      fi
      BK_STATUS="BLOCKED"
      P4_STATUS="BLOCKED"
      break
    fi
  fi

  sleep "$POLL_INTERVAL"
done

set +e
./scripts/run-physical-gates-checkpoint.sh \
  --date-tag "$DATE_TAG" \
  --summary-report "$CHECKPOINT_REPORT" \
  --bk-decision-report "$BK_DECISION_REPORT" \
  --wear-p4-report "$WEAR_REPORT"
checkpoint_code=$?
set -e

cat > "$ORCHESTRATOR_REPORT" <<EOF
# Physical Gates Orchestrator Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}
- poll_interval: ${POLL_INTERVAL}s
- timeout: ${TIMEOUT_SECONDS}s

## 오케스트레이션 결과
- BK gate status: ${BK_STATUS}
- BK serial used: ${BK_SERIAL_USED:-N/A}
- Wear P-004 status: ${P4_STATUS}
- Wear serials used: ${P4_SMALL_USED:-N/A}, ${P4_LARGE_USED:-N/A}
- checkpoint exit code: ${checkpoint_code}

## 산출물
- checkpoint summary: ${CHECKPOINT_REPORT}
- BK decision: ${BK_DECISION_REPORT}
- Wear P-004 report: ${WEAR_REPORT}
EOF

echo "[INFO] orchestrator report: $ORCHESTRATOR_REPORT"

if [[ "$checkpoint_code" -eq 0 ]]; then
  echo "[PASS] All physical gates completed."
  exit 0
fi
if [[ "$checkpoint_code" -eq 2 ]]; then
  echo "[WARN] Physical gates remain blocked."
  exit 2
fi
echo "[FAIL] Physical gates orchestration failed."
exit 1
