#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
CHECKPOINT_REPORT=""
PROJECT_ID="${FIREBASE_PROJECT_ID:-}"
APP_ID="${FIREBASE_APP_ID:-}"
GROUPS="${FIREBASE_TESTER_GROUP_ALIAS:-}"
SERVICE_ACCOUNT="${GOOGLE_APPLICATION_CREDENTIALS:-}"
RELEASE_NOTES=""
REPORT_FILE=""
SKIP_FINAL_CHECK=0
DRY_RUN_ONLY=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-physical-pass-firebase-chain.sh [options]

Options:
  --date-tag <yyyy-mm-dd>          date tag for report paths.
  --checkpoint-report <path>       physical gates checkpoint report path.
  --project-id <id>                Firebase project id (or FIREBASE_PROJECT_ID).
  --app-id <id>                    Firebase app id (or FIREBASE_APP_ID).
  --groups <aliases>               tester group aliases (or FIREBASE_TESTER_GROUP_ALIAS).
  --service-account <path>         service account json path (or GOOGLE_APPLICATION_CREDENTIALS).
  --release-notes <text>           release note text.
  --report-file <path>             output markdown report path.
  --skip-final-check               skip release-final-check (not recommended).
  --dry-run-only                   run only firebase dry-run without actual distribute.
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
      GROUPS="${2:-}"
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
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --skip-final-check)
      SKIP_FINAL_CHECK=1
      shift 1
      ;;
    --dry-run-only)
      DRY_RUN_ONLY=1
      shift 1
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
if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/firebase_physical_pass_chain_${DATE_TAG}.md"
fi

if [[ ! -f "$CHECKPOINT_REPORT" ]]; then
  echo "[FAIL] checkpoint report not found: $CHECKPOINT_REPORT"
  exit 1
fi

bk_line="$(rg -n "^- BK gate \\(BK-001/BK-002\\):" "$CHECKPOINT_REPORT" || true)"
wear_line="$(rg -n "^- Wear P-004 gate:" "$CHECKPOINT_REPORT" || true)"
if [[ -z "$bk_line" || -z "$wear_line" ]]; then
  echo "[FAIL] unable to parse checkpoint status lines: $CHECKPOINT_REPORT"
  exit 1
fi
if [[ "$bk_line" != *"PASS"* || "$wear_line" != *"PASS"* ]]; then
  echo "[FAIL] checkpoint is not fully PASS. BK/Wear must both be PASS."
  echo "       BK: $bk_line"
  echo "       P4: $wear_line"
  exit 1
fi

if [[ -z "$PROJECT_ID" || -z "$APP_ID" || -z "$GROUPS" ]]; then
  echo "[FAIL] missing firebase params: --project-id, --app-id, --groups"
  exit 1
fi
if [[ -z "$SERVICE_ACCOUNT" || ! -f "$SERVICE_ACCOUNT" ]]; then
  echo "[FAIL] service account json not found. Use --service-account."
  exit 1
fi

if [[ -z "$RELEASE_NOTES" ]]; then
  RELEASE_NOTES="Physical PASS chain (${DATE_TAG}) / commit $(git rev-parse --short HEAD)"
fi

FINAL_CHECK_STATUS="SKIPPED"
DRY_RUN_STATUS="PENDING"
DISTRIBUTE_STATUS="PENDING"
OVERALL_STATUS="PASS"

run_step() {
  local step_name="$1"
  shift
  echo "[INFO] ${step_name}"
  "$@"
}

if [[ "$SKIP_FINAL_CHECK" -eq 0 ]]; then
  if run_step "run release-final-check (physical required)" \
      ./scripts/release-final-check.sh --require-physical-device; then
    FINAL_CHECK_STATUS="PASS"
  else
    FINAL_CHECK_STATUS="FAIL"
    OVERALL_STATUS="FAIL"
  fi
fi

if [[ "$OVERALL_STATUS" == "PASS" ]]; then
  if [[ ! -f "app/build/outputs/apk/release/app-release.apk" ]]; then
    run_step "assemble release apk once" ./gradlew :app:assembleRelease
  fi

  dry_cmd=(
    ./scripts/firebase-distribute.sh
    --project-id "$PROJECT_ID"
    --app-id "$APP_ID"
    --service-account "$SERVICE_ACCOUNT"
    --groups "$GROUPS"
    --no-build
    --apk-path app/build/outputs/apk/release/app-release.apk
    --release-notes "$RELEASE_NOTES"
    --dry-run
  )
  if run_step "run firebase distribute dry-run" "${dry_cmd[@]}"; then
    DRY_RUN_STATUS="PASS"
  else
    DRY_RUN_STATUS="FAIL"
    OVERALL_STATUS="FAIL"
  fi
fi

if [[ "$OVERALL_STATUS" == "PASS" && "$DRY_RUN_ONLY" -eq 0 ]]; then
  dist_cmd=(
    ./scripts/firebase-distribute.sh
    --project-id "$PROJECT_ID"
    --app-id "$APP_ID"
    --service-account "$SERVICE_ACCOUNT"
    --groups "$GROUPS"
    --no-build
    --apk-path app/build/outputs/apk/release/app-release.apk
    --release-notes "$RELEASE_NOTES"
  )
  if run_step "run firebase distribute actual upload" "${dist_cmd[@]}"; then
    DISTRIBUTE_STATUS="PASS"
  else
    DISTRIBUTE_STATUS="FAIL"
    OVERALL_STATUS="FAIL"
  fi
elif [[ "$DRY_RUN_ONLY" -eq 1 ]]; then
  DISTRIBUTE_STATUS="SKIPPED_DRY_RUN_ONLY"
fi

mkdir -p "$(dirname "$REPORT_FILE")"
cat > "$REPORT_FILE" <<EOF
# Firebase Physical PASS Chain Report

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- date_tag: ${DATE_TAG}
- checkpoint_report: ${CHECKPOINT_REPORT}
- project_id: ${PROJECT_ID}
- app_id: ${APP_ID}
- groups: ${GROUPS}

## Step Results
- final_check: ${FINAL_CHECK_STATUS}
- firebase_dry_run: ${DRY_RUN_STATUS}
- firebase_distribute: ${DISTRIBUTE_STATUS}

## Overall
- status: ${OVERALL_STATUS}
EOF

echo "[INFO] chain report: $REPORT_FILE"

if [[ "$OVERALL_STATUS" != "PASS" ]]; then
  echo "[FAIL] physical PASS -> firebase chain failed."
  exit 1
fi

echo "[PASS] physical PASS -> firebase chain completed."
