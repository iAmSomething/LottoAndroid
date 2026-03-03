#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
POLL_INTERVAL=30
TIMEOUT_SECONDS=0
CHECKPOINT_REPORT=""
PROJECT_ID="${FIREBASE_PROJECT_ID:-}"
APP_ID="${FIREBASE_APP_ID:-}"
TESTER_GROUPS="${FIREBASE_TESTER_GROUP_ALIAS:-}"
SERVICE_ACCOUNT="${GOOGLE_APPLICATION_CREDENTIALS:-}"
RELEASE_NOTES=""
CHAIN_REPORT=""
RUNNER_REPORT=""
TARGET_SERIAL=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-bo005-when-physical-ready.sh [options]

Required:
  --project-id <id>                Firebase project id (or FIREBASE_PROJECT_ID).
  --app-id <id>                    Firebase app id (or FIREBASE_APP_ID).
  --groups <aliases>               tester group aliases (or FIREBASE_TESTER_GROUP_ALIAS).
  --service-account <path>         service account json path (or GOOGLE_APPLICATION_CREDENTIALS).

Options:
  --date-tag <yyyy-mm-dd>          date tag used for report paths.
  --checkpoint-report <path>       physical gates checkpoint report path.
  --release-notes <text>           release note text.
  --chain-report <path>            run-physical-pass-firebase-chain report path.
  --runner-report <path>           readiness/attempt summary report path.
  --serial <adb-serial>            target physical serial. If omitted, exactly one physical serial is required.
  --poll-interval <seconds>        readiness polling interval (default: 30).
  --timeout-seconds <seconds>      timeout (0 means infinite).
  -h, --help                       show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
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
    --chain-report)
      CHAIN_REPORT="${2:-}"
      shift 2
      ;;
    --runner-report)
      RUNNER_REPORT="${2:-}"
      shift 2
      ;;
    --serial)
      TARGET_SERIAL="${2:-}"
      shift 2
      ;;
    --poll-interval)
      POLL_INTERVAL="${2:-30}"
      shift 2
      ;;
    --timeout-seconds)
      TIMEOUT_SECONDS="${2:-0}"
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

if [[ -z "$CHECKPOINT_REPORT" ]]; then
  CHECKPOINT_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
fi
if [[ -z "$CHAIN_REPORT" ]]; then
  CHAIN_REPORT="docs/assets/distribution/firebase_physical_pass_chain_bo005_${DATE_TAG}.md"
fi
if [[ -z "$RUNNER_REPORT" ]]; then
  RUNNER_REPORT="docs/assets/distribution/bo005_when_physical_ready_${DATE_TAG}.md"
fi

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
if [[ -z "$PROJECT_ID" || -z "$APP_ID" || -z "$TESTER_GROUPS" ]]; then
  echo "[FAIL] missing firebase params: --project-id, --app-id, --groups"
  exit 1
fi
if [[ -z "$SERVICE_ACCOUNT" || ! -f "$SERVICE_ACCOUNT" ]]; then
  echo "[FAIL] service account json not found. Use --service-account."
  exit 1
fi

mkdir -p "$(dirname "$RUNNER_REPORT")"
mkdir -p "$(dirname "$CHAIN_REPORT")"

checkpoint_exists=0
checkpoint_bk_pass=0
checkpoint_wear_pass=0
checkpoint_ready=0
checkpoint_bk_line="N/A"
checkpoint_wear_line="N/A"
physical_count=0
selected_serial=""
serial_reason=""

update_checkpoint_status() {
  checkpoint_exists=0
  checkpoint_bk_pass=0
  checkpoint_wear_pass=0
  checkpoint_ready=0
  checkpoint_bk_line="N/A"
  checkpoint_wear_line="N/A"

  if [[ ! -f "$CHECKPOINT_REPORT" ]]; then
    return 0
  fi

  checkpoint_exists=1
  checkpoint_bk_line="$(rg -n "^- BK gate \(BK-001/BK-002\):" "$CHECKPOINT_REPORT" || true)"
  checkpoint_wear_line="$(rg -n "^- Wear P-004 gate:" "$CHECKPOINT_REPORT" || true)"

  if [[ "$checkpoint_bk_line" == *"PASS"* ]]; then
    checkpoint_bk_pass=1
  fi
  if [[ "$checkpoint_wear_line" == *"PASS"* ]]; then
    checkpoint_wear_pass=1
  fi
  if [[ "$checkpoint_bk_pass" -eq 1 && "$checkpoint_wear_pass" -eq 1 ]]; then
    checkpoint_ready=1
  fi
}

update_serial_status() {
  local -a physical_serials=()
  local serial
  while IFS= read -r serial; do
    [[ -n "$serial" ]] || continue
    physical_serials+=("$serial")
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')

  physical_count="${#physical_serials[@]}"
  selected_serial=""
  serial_reason=""

  if [[ -n "$TARGET_SERIAL" ]]; then
    local found=0
    local serial
    for serial in "${physical_serials[@]}"; do
      if [[ "$serial" == "$TARGET_SERIAL" ]]; then
        found=1
        break
      fi
    done
    if [[ "$found" -eq 1 ]]; then
      selected_serial="$TARGET_SERIAL"
    else
      serial_reason="target serial not connected: ${TARGET_SERIAL}"
    fi
    return 0
  fi

  if [[ "$physical_count" -eq 1 ]]; then
    selected_serial="${physical_serials[0]}"
    return 0
  fi
  if [[ "$physical_count" -eq 0 ]]; then
    serial_reason="no physical device connected"
  else
    serial_reason="multiple physical devices connected (${physical_count}); use --serial"
  fi
}

started_at_epoch="$(date +%s)"
started_at_text="$(date '+%Y-%m-%d %H:%M:%S %z')"

echo "[INFO] waiting for BO-005 readiness..."
echo "       date_tag=${DATE_TAG} poll=${POLL_INTERVAL}s timeout=${TIMEOUT_SECONDS}s"
echo "       checkpoint=${CHECKPOINT_REPORT}"

while true; do
  update_checkpoint_status
  update_serial_status

  if [[ -n "$selected_serial" && "$checkpoint_ready" -eq 1 ]]; then
    break
  fi

  if [[ "$TIMEOUT_SECONDS" -gt 0 ]]; then
    now_epoch="$(date +%s)"
    elapsed="$((now_epoch - started_at_epoch))"
    if [[ "$elapsed" -ge "$TIMEOUT_SECONDS" ]]; then
      cat > "$RUNNER_REPORT" <<EOF
# BO-005 Physical Ready Runner Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}
- result: BLOCKED_TIMEOUT
- elapsed_seconds: ${elapsed}

## Readiness Snapshot
- checkpoint_report: ${CHECKPOINT_REPORT}
- checkpoint_exists: ${checkpoint_exists}
- checkpoint_bk_pass: ${checkpoint_bk_pass}
- checkpoint_wear_pass: ${checkpoint_wear_pass}
- checkpoint_bk_line: ${checkpoint_bk_line}
- checkpoint_wear_line: ${checkpoint_wear_line}
- physical_device_count: ${physical_count}
- selected_serial: ${selected_serial:-N/A}
- serial_reason: ${serial_reason:-N/A}

## Next Action
- checkpoint와 실기기 조건 충족 후 동일 명령으로 재실행한다.
EOF
      echo "[WARN] timeout reached. blocked report written: $RUNNER_REPORT"
      exit 2
    fi
  fi

  sleep "$POLL_INTERVAL"
done

chain_cmd=(
  ./scripts/run-physical-pass-firebase-chain.sh
  --date-tag "$DATE_TAG"
  --checkpoint-report "$CHECKPOINT_REPORT"
  --project-id "$PROJECT_ID"
  --app-id "$APP_ID"
  --groups "$TESTER_GROUPS"
  --service-account "$SERVICE_ACCOUNT"
  --report-file "$CHAIN_REPORT"
  --serial "$selected_serial"
)
if [[ -n "$RELEASE_NOTES" ]]; then
  chain_cmd+=(--release-notes "$RELEASE_NOTES")
fi

echo "[INFO] readiness satisfied. run chain command"

after_ready_text="$(date '+%Y-%m-%d %H:%M:%S %z')"
set +e
"${chain_cmd[@]}"
chain_code=$?
set -e

chain_result="FAIL"
if [[ "$chain_code" -eq 0 ]]; then
  chain_result="PASS"
fi

cat > "$RUNNER_REPORT" <<EOF
# BO-005 Physical Ready Runner Report

- 시작 시각: ${started_at_text}
- 실행 시각: ${after_ready_text}
- date_tag: ${DATE_TAG}
- result: ${chain_result}
- chain_exit_code: ${chain_code}

## Readiness Snapshot (at run)
- checkpoint_report: ${CHECKPOINT_REPORT}
- checkpoint_exists: ${checkpoint_exists}
- checkpoint_bk_pass: ${checkpoint_bk_pass}
- checkpoint_wear_pass: ${checkpoint_wear_pass}
- checkpoint_bk_line: ${checkpoint_bk_line}
- checkpoint_wear_line: ${checkpoint_wear_line}
- physical_device_count: ${physical_count}
- selected_serial: ${selected_serial}

## Chain Artifacts
- chain_report: ${CHAIN_REPORT}

## Executed Command
- $(printf '%q ' "${chain_cmd[@]}")
EOF

echo "[INFO] runner report: $RUNNER_REPORT"
exit "$chain_code"
