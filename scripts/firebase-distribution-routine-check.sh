#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${FIREBASE_PROJECT_ID:-}"
APP_ID="${FIREBASE_APP_ID:-}"
GROUP_ALIAS="${FIREBASE_TESTER_GROUP_ALIAS:-}"
GROUP_DISPLAY_NAME="${FIREBASE_TESTER_GROUP_DISPLAY_NAME:-}"
SERVICE_ACCOUNT_PATH=""
SERVICE_ACCOUNT_BASE64="${FIREBASE_SERVICE_ACCOUNT_JSON_BASE64:-}"
REPORT_FILE=""
RELEASE_NOTES=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/firebase-distribution-routine-check.sh [options]

Options:
  --project-id <id>            Firebase project id (or FIREBASE_PROJECT_ID)
  --app-id <id>                Firebase app id (or FIREBASE_APP_ID)
  --groups <aliases>           Firebase tester group aliases (or FIREBASE_TESTER_GROUP_ALIAS)
  --group-display-name <name>  Optional display name for group create check
  --service-account <path>     Service account JSON path
  --service-account-base64 <b> Service account JSON base64 string
  --release-notes <text>       Dry-run release note text
  --report-file <path>         Write markdown report file
  -h, --help                   Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project-id)
      PROJECT_ID="${2:-}"
      shift 2
      ;;
    --app-id)
      APP_ID="${2:-}"
      shift 2
      ;;
    --groups)
      GROUP_ALIAS="${2:-}"
      shift 2
      ;;
    --group-display-name)
      GROUP_DISPLAY_NAME="${2:-}"
      shift 2
      ;;
    --service-account)
      SERVICE_ACCOUNT_PATH="${2:-}"
      shift 2
      ;;
    --service-account-base64)
      SERVICE_ACCOUNT_BASE64="${2:-}"
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

if [[ -z "$PROJECT_ID" ]]; then
  echo "[FAIL] Missing project id. Set --project-id or FIREBASE_PROJECT_ID."
  exit 1
fi
if [[ -z "$APP_ID" ]]; then
  echo "[FAIL] Missing app id. Set --app-id or FIREBASE_APP_ID."
  exit 1
fi
if [[ -z "$GROUP_ALIAS" ]]; then
  echo "[FAIL] Missing tester groups. Set --groups or FIREBASE_TESTER_GROUP_ALIAS."
  exit 1
fi

TMP_DIR="$(mktemp -d)"
cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

resolve_service_account() {
  if [[ -n "$SERVICE_ACCOUNT_PATH" ]]; then
    if [[ ! -f "$SERVICE_ACCOUNT_PATH" ]]; then
      echo "[FAIL] Service account file not found: $SERVICE_ACCOUNT_PATH"
      exit 1
    fi
    echo "$SERVICE_ACCOUNT_PATH"
    return
  fi

  if [[ -n "$SERVICE_ACCOUNT_BASE64" ]]; then
    local target="$TMP_DIR/firebase-service-account.json"
    printf '%s' "$SERVICE_ACCOUNT_BASE64" | base64 --decode > "$target"
    echo "$target"
    return
  fi

  if [[ -n "${GOOGLE_APPLICATION_CREDENTIALS:-}" && -f "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]]; then
    echo "$GOOGLE_APPLICATION_CREDENTIALS"
    return
  fi

  echo "[FAIL] Missing service account. Use --service-account, --service-account-base64, or GOOGLE_APPLICATION_CREDENTIALS." >&2
  exit 1
}

prepare_signing_env() {
  if [[ -n "${LOTTO_RELEASE_STORE_FILE:-}" && -f "${LOTTO_RELEASE_STORE_FILE:-}" ]]; then
    return
  fi

  if [[ -z "${LOTTO_RELEASE_STORE_FILE_BASE64:-}" ]]; then
    return
  fi

  : "${LOTTO_RELEASE_STORE_PASSWORD:?Missing LOTTO_RELEASE_STORE_PASSWORD}"
  : "${LOTTO_RELEASE_KEY_ALIAS:?Missing LOTTO_RELEASE_KEY_ALIAS}"
  : "${LOTTO_RELEASE_KEY_PASSWORD:?Missing LOTTO_RELEASE_KEY_PASSWORD}"

  local store_file="$TMP_DIR/weeklylotto-release.jks"
  printf '%s' "$LOTTO_RELEASE_STORE_FILE_BASE64" | base64 --decode > "$store_file"
  export LOTTO_RELEASE_STORE_FILE="$store_file"
}

SERVICE_ACCOUNT="$(resolve_service_account)"
prepare_signing_env

if [[ -z "$RELEASE_NOTES" ]]; then
  RELEASE_NOTES="Routine dry-run: $(date '+%Y-%m-%d %H:%M %z')"
fi

echo "[INFO] Run release preflight CI gate"
./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing

echo "[INFO] Run Firebase distribution dry-run"
cmd=(
  ./scripts/firebase-distribute.sh
  --project-id "$PROJECT_ID"
  --app-id "$APP_ID"
  --service-account "$SERVICE_ACCOUNT"
  --groups "$GROUP_ALIAS"
  --no-build
  --apk-path app/build/outputs/apk/release/app-release.apk
  --release-notes "$RELEASE_NOTES"
  --dry-run
)
if [[ -n "$GROUP_DISPLAY_NAME" ]]; then
  cmd+=(--group-display-name "$GROUP_DISPLAY_NAME" --group-alias "$GROUP_ALIAS")
fi
"${cmd[@]}"

if [[ -n "$REPORT_FILE" ]]; then
  mkdir -p "$(dirname "$REPORT_FILE")"
  {
    echo "# Firebase Distribution Routine Check"
    echo
    echo "- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')"
    echo "- project: \`$PROJECT_ID\`"
    echo "- app: \`$APP_ID\`"
    echo "- groups: \`$GROUP_ALIAS\`"
    echo "- preflight: PASS (\`--with-build-ci --skip-adb --require-signing\`)"
    echo "- distribution dry-run: PASS"
    echo '- note: 실제 업로드는 수행하지 않음(`--dry-run`)'
  } > "$REPORT_FILE"
  echo "[INFO] Routine report written: $REPORT_FILE"
fi

echo "[PASS] Firebase distribution routine check completed."
