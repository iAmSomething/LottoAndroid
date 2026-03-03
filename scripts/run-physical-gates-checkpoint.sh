#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
SUMMARY_REPORT=""
BK_DECISION_REPORT=""
WEAR_P4_REPORT=""
EMULATOR_REPORT=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-physical-gates-checkpoint.sh [options]

Options:
  --date-tag <yyyy-mm-dd>          report date tag.
  --summary-report <path>          consolidated summary report path.
  --bk-decision-report <path>      BK decision report path.
  --wear-p4-report <path>          Wear P-004 report path.
  --emulator-report <path>         emulator report path for BK evaluation.
  -h, --help                       show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --summary-report)
      SUMMARY_REPORT="${2:-}"
      shift 2
      ;;
    --bk-decision-report)
      BK_DECISION_REPORT="${2:-}"
      shift 2
      ;;
    --wear-p4-report)
      WEAR_P4_REPORT="${2:-}"
      shift 2
      ;;
    --emulator-report)
      EMULATOR_REPORT="${2:-}"
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

if [[ -z "$SUMMARY_REPORT" ]]; then
  SUMMARY_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
fi
if [[ -z "$BK_DECISION_REPORT" ]]; then
  BK_DECISION_REPORT="docs/assets/distribution/performance_release_decision_checkpoint_${DATE_TAG}.md"
fi
if [[ -z "$WEAR_P4_REPORT" ]]; then
  WEAR_P4_REPORT="docs/assets/distribution/wear_p4_device_evidence_checkpoint_${DATE_TAG}.md"
fi

mkdir -p "$(dirname "$SUMMARY_REPORT")"

if [[ ! -x "./scripts/run-bk-device-gate.sh" ]]; then
  echo "[FAIL] Missing executable: ./scripts/run-bk-device-gate.sh"
  exit 1
fi
if [[ ! -x "./scripts/run-p4-wear-proof-gate.sh" ]]; then
  echo "[FAIL] Missing executable: ./scripts/run-p4-wear-proof-gate.sh"
  exit 1
fi

run_bk_gate() {
  local cmd=(./scripts/run-bk-device-gate.sh --date-tag "$DATE_TAG" --decision-report "$BK_DECISION_REPORT" --save-blocked-report)
  if [[ -n "$EMULATOR_REPORT" ]]; then
    cmd+=(--emulator-report "$EMULATOR_REPORT")
  fi
  set +e
  "${cmd[@]}" >/dev/stderr 2>&1
  local code=$?
  set -e
  echo "$code"
}

run_wear_p4_gate() {
  local cmd=(./scripts/run-p4-wear-proof-gate.sh --skip-install --date-tag "$DATE_TAG" --report-path "$WEAR_P4_REPORT" --save-blocked-report)
  set +e
  "${cmd[@]}" >/dev/stderr 2>&1
  local code=$?
  set -e
  echo "$code"
}

map_status() {
  local code="$1"
  case "$code" in
    0) echo "PASS" ;;
    2) echo "BLOCKED" ;;
    *) echo "FAIL" ;;
  esac
}

BK_CODE="$(run_bk_gate)"
P4_CODE="$(run_wear_p4_gate)"

BK_STATUS="$(map_status "$BK_CODE")"
P4_STATUS="$(map_status "$P4_CODE")"

cat > "$SUMMARY_REPORT" <<EOF
# Physical Gates Checkpoint Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}

## 결과 요약
- BK gate (BK-001/BK-002): ${BK_STATUS} (code=${BK_CODE})
- Wear P-004 gate: ${P4_STATUS} (code=${P4_CODE})

## 산출물
- BK decision report: ${BK_DECISION_REPORT}
- Wear P-004 report: ${WEAR_P4_REPORT}

## 후속 액션
1. 폰 실기기 연결 후 ./scripts/run-bk-when-physical.sh --date-tag ${DATE_TAG}
2. Wear 소형/대형 실기기 연결 후 ./scripts/run-p4-when-wear-physical.sh --date-tag ${DATE_TAG}
3. 두 리포트가 PASS로 전환되면 docs/10-detailed-todo-board.md의 BK-001, BK-002, P-004 완료 처리
EOF

echo "[INFO] checkpoint report: $SUMMARY_REPORT"
echo "[INFO] bk report: $BK_DECISION_REPORT"
echo "[INFO] wear report: $WEAR_P4_REPORT"

if [[ "$BK_STATUS" == "FAIL" || "$P4_STATUS" == "FAIL" ]]; then
  echo "[FAIL] Physical gates checkpoint has failure."
  exit 1
fi
if [[ "$BK_STATUS" == "BLOCKED" || "$P4_STATUS" == "BLOCKED" ]]; then
  echo "[WARN] Physical gates checkpoint is blocked."
  exit 2
fi

echo "[PASS] Physical gates checkpoint passed."
