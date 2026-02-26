#!/usr/bin/env bash
set -euo pipefail

APP_ID=""
PROJECT_ID=""
APK_PATH="app/build/outputs/apk/release/app-release.apk"
BUILD_TASK=":app:assembleRelease"
NO_BUILD=0
TESTER_GROUPS=""
TESTERS=""
GROUP_DISPLAY_NAME=""
GROUP_ALIAS=""
SERVICE_ACCOUNT=""
RELEASE_NOTES=""
RELEASE_NOTES_FILE=""
DRY_RUN=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/firebase-distribute.sh --app-id <firebase-app-id> [options]

Required:
  --app-id <id>                Firebase App ID (e.g. 1:123:android:abc)

At least one target:
  --groups <aliases>           Comma separated tester group aliases
  --testers <emails>           Comma separated tester emails

Options:
  --project-id <id>            Firebase project id
  --apk-path <path>            APK path (default: app/build/outputs/apk/release/app-release.apk)
  --build-task <gradle-task>   Gradle task before distribute (default: :app:assembleRelease)
  --no-build                   Skip Gradle build step
  --service-account <path>     Service account json path (exports GOOGLE_APPLICATION_CREDENTIALS)
  --release-notes <text>       Inline release notes
  --release-notes-file <path>  File path for release notes
  --group-display-name <name>  Create group before distribute (display name)
  --group-alias <alias>        Group alias for create/distribute
  --dry-run                    Validate inputs and print distribution command without upload
  -h, --help                   Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --app-id)
      APP_ID="${2:-}"
      shift 2
      ;;
    --project-id)
      PROJECT_ID="${2:-}"
      shift 2
      ;;
    --apk-path)
      APK_PATH="${2:-}"
      shift 2
      ;;
    --build-task)
      BUILD_TASK="${2:-}"
      shift 2
      ;;
    --no-build)
      NO_BUILD=1
      shift
      ;;
    --groups)
      TESTER_GROUPS="${2:-}"
      shift 2
      ;;
    --testers)
      TESTERS="${2:-}"
      shift 2
      ;;
    --group-display-name)
      GROUP_DISPLAY_NAME="${2:-}"
      shift 2
      ;;
    --group-alias)
      GROUP_ALIAS="${2:-}"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    --service-account)
      SERVICE_ACCOUNT="${2:-}"
      shift 2
      ;;
    --release-notes)
      RELEASE_NOTES="${2:-}"
      shift 2
      ;;
    --release-notes-file)
      RELEASE_NOTES_FILE="${2:-}"
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

if [[ -z "$APP_ID" ]]; then
  echo "[FAIL] --app-id is required."
  usage
  exit 1
fi

if [[ -n "$SERVICE_ACCOUNT" ]]; then
  export GOOGLE_APPLICATION_CREDENTIALS="$SERVICE_ACCOUNT"
fi

if [[ -z "${GOOGLE_APPLICATION_CREDENTIALS:-}" ]]; then
  echo "[FAIL] GOOGLE_APPLICATION_CREDENTIALS is not set. Use --service-account."
  exit 1
fi

if [[ ! -f "${GOOGLE_APPLICATION_CREDENTIALS}" ]]; then
  echo "[FAIL] Service account file not found: ${GOOGLE_APPLICATION_CREDENTIALS}"
  exit 1
fi

FIREBASE_CMD=(firebase)
if [[ -n "$PROJECT_ID" ]]; then
  FIREBASE_CMD+=(--project "$PROJECT_ID")
fi

if [[ -n "$GROUP_DISPLAY_NAME" ]]; then
  if [[ -z "$GROUP_ALIAS" ]]; then
    echo "[FAIL] --group-display-name requires --group-alias."
    exit 1
  fi

  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "[INFO] Dry-run: skip tester group creation (${GROUP_DISPLAY_NAME}/${GROUP_ALIAS})"
  else
    echo "[INFO] Ensure tester group exists: ${GROUP_DISPLAY_NAME} (${GROUP_ALIAS})"
    set +e
    group_out="$("${FIREBASE_CMD[@]}" appdistribution:group:create "$GROUP_DISPLAY_NAME" "$GROUP_ALIAS" 2>&1)"
    group_code=$?
    set -e
    if [[ $group_code -ne 0 ]]; then
      if [[ "$group_out" == *"already exists"* ]] || [[ "$group_out" == *"ALREADY_EXISTS"* ]]; then
        echo "[INFO] Group already exists: ${GROUP_ALIAS}"
      else
        echo "$group_out"
        echo "[FAIL] Failed to create tester group."
        exit $group_code
      fi
    fi
  fi
  if [[ -z "$TESTER_GROUPS" ]]; then
    TESTER_GROUPS="$GROUP_ALIAS"
  fi
fi

if [[ -z "$TESTER_GROUPS" && -n "$GROUP_ALIAS" ]]; then
  TESTER_GROUPS="$GROUP_ALIAS"
fi

if [[ -z "$TESTER_GROUPS" && -z "$TESTERS" ]]; then
  echo "[FAIL] Provide --groups or --testers."
  exit 1
fi

if [[ "$NO_BUILD" -eq 0 ]]; then
  echo "[INFO] Run build task: ./gradlew ${BUILD_TASK}"
  ./gradlew "$BUILD_TASK"
fi

if [[ ! -f "$APK_PATH" ]]; then
  echo "[FAIL] APK not found: $APK_PATH"
  exit 1
fi

DIST_CMD=("${FIREBASE_CMD[@]}" appdistribution:distribute "$APK_PATH" --app "$APP_ID")
if [[ -n "$TESTER_GROUPS" ]]; then
  DIST_CMD+=(--groups "$TESTER_GROUPS")
fi
if [[ -n "$TESTERS" ]]; then
  DIST_CMD+=(--testers "$TESTERS")
fi
if [[ -n "$RELEASE_NOTES_FILE" ]]; then
  DIST_CMD+=(--release-notes-file "$RELEASE_NOTES_FILE")
else
  if [[ -z "$RELEASE_NOTES" ]]; then
    commit_short="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
    timestamp="$(date '+%Y-%m-%d %H:%M')"
    RELEASE_NOTES="자동 배포 (${timestamp}) / commit ${commit_short}"
  fi
  DIST_CMD+=(--release-notes "$RELEASE_NOTES")
fi

echo "[INFO] Distribute APK to Firebase App Distribution"
echo "  - app: $APP_ID"
echo "  - apk: $APK_PATH"
if [[ -n "$TESTER_GROUPS" ]]; then
  echo "  - groups: $TESTER_GROUPS"
fi
if [[ -n "$TESTERS" ]]; then
  echo "  - testers: $TESTERS"
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "[INFO] Dry-run command:"
  printf '  %q' "${DIST_CMD[@]}"
  printf '\n'
  echo "[PASS] Firebase distribution dry-run completed."
else
  "${DIST_CMD[@]}"
  echo "[PASS] Firebase distribution completed."
fi
