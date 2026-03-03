#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
REPORT_FILE=""
TIMEOUT_SECONDS=1
POLL_INTERVAL=1

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-physical-gates-routine-check.sh [options]

Options:
  --date-tag <yyyy-mm-dd>      report date tag.
  --report-file <path>         output report file path.
  --timeout-seconds <sec>      orchestrator timeout (default: 1).
  --poll-interval <sec>        orchestrator poll interval (default: 1).
  -h, --help                   show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --timeout-seconds)
      TIMEOUT_SECONDS="${2:-1}"
      shift 2
      ;;
    --poll-interval)
      POLL_INTERVAL="${2:-1}"
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

if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/physical_gates_routine_${DATE_TAG}.md"
fi

if [[ ! -x "./scripts/run-all-physical-gates-when-ready.sh" ]]; then
  echo "[FAIL] Missing executable: ./scripts/run-all-physical-gates-when-ready.sh"
  exit 1
fi

ORCH_REPORT="docs/assets/distribution/physical_gates_orchestrator_${DATE_TAG}.md"
CHECKPOINT_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
BK_REPORT="docs/assets/distribution/performance_release_decision_checkpoint_${DATE_TAG}.md"
WEAR_REPORT="docs/assets/distribution/wear_p4_device_evidence_checkpoint_${DATE_TAG}.md"

mkdir -p "$(dirname "$REPORT_FILE")"

set +e
./scripts/run-all-physical-gates-when-ready.sh \
  --date-tag "$DATE_TAG" \
  --timeout-seconds "$TIMEOUT_SECONDS" \
  --poll-interval "$POLL_INTERVAL" \
  --save-blocked-report \
  --orchestrator-report "$ORCH_REPORT" \
  --checkpoint-report "$CHECKPOINT_REPORT" \
  --bk-decision-report "$BK_REPORT" \
  --wear-report "$WEAR_REPORT"
ORCH_EXIT=$?
set -e

ROUTINE_STATUS="FAIL"
case "$ORCH_EXIT" in
  0) ROUTINE_STATUS="PASS" ;;
  2) ROUTINE_STATUS="BLOCKED" ;;
  *) ROUTINE_STATUS="FAIL" ;;
esac

cat > "$REPORT_FILE" <<EOF
# Physical Gates Routine Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}
- orchestrator_exit: ${ORCH_EXIT}
- routine_status: ${ROUTINE_STATUS}

## 산출물
- orchestrator_report: ${ORCH_REPORT}
- checkpoint_report: ${CHECKPOINT_REPORT}
- bk_decision_report: ${BK_REPORT}
- wear_p4_report: ${WEAR_REPORT}

## 운영 규칙
1. routine_status=PASS: 즉시 ./scripts/sync-physical-blockers-from-checkpoint.sh --date-tag ${DATE_TAG} --apply 실행
2. routine_status=BLOCKED: 실기기 연결 후 오케스트레이터 재실행
3. routine_status=FAIL: 스크립트/adb 환경 오류 우선 복구
EOF

echo "[INFO] routine report: $REPORT_FILE"

if [[ "$ROUTINE_STATUS" == "FAIL" ]]; then
  exit 1
fi
exit 0
