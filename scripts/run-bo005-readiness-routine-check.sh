#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
REPORT_FILE=""
RUNNER_REPORT=""
CHAIN_REPORT=""
CHECKPOINT_REPORT=""
POLL_INTERVAL=1
TIMEOUT_SECONDS=1
PROJECT_ID="${FIREBASE_PROJECT_ID:-}"
APP_ID="${FIREBASE_APP_ID:-}"
TESTER_GROUPS="${FIREBASE_TESTER_GROUP_ALIAS:-}"
SERVICE_ACCOUNT="${GOOGLE_APPLICATION_CREDENTIALS:-}"
RELEASE_NOTES=""

TEMP_SA_FILE=""
cleanup() {
  if [[ -n "$TEMP_SA_FILE" && -f "$TEMP_SA_FILE" ]]; then
    rm -f "$TEMP_SA_FILE"
  fi
}
trap cleanup EXIT

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-bo005-readiness-routine-check.sh [options]

Required:
  --project-id <id>                Firebase project id (or FIREBASE_PROJECT_ID).
  --app-id <id>                    Firebase app id (or FIREBASE_APP_ID).
  --groups <aliases>               tester group aliases (or FIREBASE_TESTER_GROUP_ALIAS).

Options:
  --date-tag <yyyy-mm-dd>          report date tag.
  --report-file <path>             output routine report path.
  --runner-report <path>           output BO-005 runner report path.
  --chain-report <path>            output chain report path.
  --checkpoint-report <path>       physical gates checkpoint report path.
  --service-account <path>         service account json path (or GOOGLE_APPLICATION_CREDENTIALS).
  --release-notes <text>           release note text.
  --timeout-seconds <sec>          readiness wait timeout (default: 1).
  --poll-interval <sec>            readiness poll interval (default: 1).
  -h, --help                       show this help.
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
    --runner-report)
      RUNNER_REPORT="${2:-}"
      shift 2
      ;;
    --chain-report)
      CHAIN_REPORT="${2:-}"
      shift 2
      ;;
    --checkpoint-report)
      CHECKPOINT_REPORT="${2:-}"
      shift 2
      ;;
    --project-id)
      PROJECT_ID="${2:-}"
      shift 2
      ;;
    --app-id)
      APP_ID="${2:-}"
      shift 2
      ;;
    --groups)
      TESTER_GROUPS="${2:-}"
      shift 2
      ;;
    --service-account)
      SERVICE_ACCOUNT="${2:-}"
      shift 2
      ;;
    --release-notes)
      RELEASE_NOTES="${2:-}"
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
  REPORT_FILE="docs/assets/distribution/bo005_readiness_routine_${DATE_TAG}.md"
fi
if [[ -z "$RUNNER_REPORT" ]]; then
  RUNNER_REPORT="docs/assets/distribution/bo005_when_physical_ready_routine_${DATE_TAG}.md"
fi
if [[ -z "$CHAIN_REPORT" ]]; then
  CHAIN_REPORT="docs/assets/distribution/firebase_physical_pass_chain_bo005_routine_${DATE_TAG}.md"
fi
if [[ -z "$CHECKPOINT_REPORT" ]]; then
  CHECKPOINT_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
fi

if ! [[ "$POLL_INTERVAL" =~ ^[0-9]+$ ]] || [[ "$POLL_INTERVAL" -lt 1 ]]; then
  echo "[FAIL] --poll-interval must be a positive integer."
  exit 1
fi
if ! [[ "$TIMEOUT_SECONDS" =~ ^[0-9]+$ ]] || [[ "$TIMEOUT_SECONDS" -lt 1 ]]; then
  echo "[FAIL] --timeout-seconds must be a positive integer."
  exit 1
fi
if [[ -z "$PROJECT_ID" || -z "$APP_ID" || -z "$TESTER_GROUPS" ]]; then
  echo "[FAIL] missing firebase params: --project-id, --app-id, --groups"
  exit 1
fi
if ! command -v adb >/dev/null 2>&1; then
  echo "[FAIL] adb command not found."
  exit 1
fi
if [[ ! -x "./scripts/run-bo005-when-physical-ready.sh" ]]; then
  echo "[FAIL] Missing executable: ./scripts/run-bo005-when-physical-ready.sh"
  exit 1
fi

if [[ -z "$SERVICE_ACCOUNT" && -n "${FIREBASE_SERVICE_ACCOUNT_JSON_BASE64:-}" ]]; then
  TEMP_SA_FILE="$(mktemp "${TMPDIR:-/tmp}/bo005-sa-XXXXXX.json")"
  printf '%s' "$FIREBASE_SERVICE_ACCOUNT_JSON_BASE64" | base64 --decode > "$TEMP_SA_FILE"
  SERVICE_ACCOUNT="$TEMP_SA_FILE"
fi
if [[ -z "$SERVICE_ACCOUNT" || ! -f "$SERVICE_ACCOUNT" ]]; then
  echo "[FAIL] service account json not found. Use --service-account or FIREBASE_SERVICE_ACCOUNT_JSON_BASE64."
  exit 1
fi

mkdir -p "$(dirname "$REPORT_FILE")"

runner_cmd=(
  ./scripts/run-bo005-when-physical-ready.sh
  --date-tag "$DATE_TAG"
  --checkpoint-report "$CHECKPOINT_REPORT"
  --project-id "$PROJECT_ID"
  --app-id "$APP_ID"
  --groups "$TESTER_GROUPS"
  --service-account "$SERVICE_ACCOUNT"
  --runner-report "$RUNNER_REPORT"
  --chain-report "$CHAIN_REPORT"
  --poll-interval "$POLL_INTERVAL"
  --timeout-seconds "$TIMEOUT_SECONDS"
)
if [[ -n "$RELEASE_NOTES" ]]; then
  runner_cmd+=(--release-notes "$RELEASE_NOTES")
fi

set +e
"${runner_cmd[@]}"
RUNNER_EXIT=$?
set -e

ROUTINE_STATUS="FAIL"
if [[ "$RUNNER_EXIT" -eq 0 ]]; then
  ROUTINE_STATUS="READY_PASS"
elif [[ "$RUNNER_EXIT" -eq 2 ]]; then
  ROUTINE_STATUS="BLOCKED"
fi

cat > "$REPORT_FILE" <<__REPORT__
# BO-005 Readiness Routine Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}
- routine_status: ${ROUTINE_STATUS}
- runner_exit: ${RUNNER_EXIT}

## Inputs
- checkpoint_report: ${CHECKPOINT_REPORT}
- project_id: ${PROJECT_ID}
- app_id: ${APP_ID}
- groups: ${TESTER_GROUPS}
- poll_interval: ${POLL_INTERVAL}
- timeout_seconds: ${TIMEOUT_SECONDS}

## Artifacts
- routine_report: ${REPORT_FILE}
- runner_report: ${RUNNER_REPORT}
- chain_report: ${CHAIN_REPORT}

## Executed Command
- $(printf '%q ' "${runner_cmd[@]}")

## Routine Policy
1. routine_status=READY_PASS: BO-005 성공 증적 후보로 검토
2. routine_status=BLOCKED: 실기기 미연결/조건 미충족 상태로 다음 주기 재실행
3. routine_status=FAIL: 스크립트/환경 오류 우선 복구
__REPORT__

echo "[INFO] routine report: $REPORT_FILE"

if [[ "$ROUTINE_STATUS" == "FAIL" ]]; then
  exit 1
fi
exit 0
