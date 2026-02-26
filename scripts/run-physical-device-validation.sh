#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

SERIAL=""
SKIP_DOC=0

usage() {
  cat <<EOF
Usage: $0 [--serial <adb-serial>] [--skip-doc]

Options:
  --serial <adb-serial>  Use a specific physical device serial.
  --skip-doc             Do not append run result to docs/18-device-validation-report.md.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --serial"
        usage
        exit 2
      fi
      SERIAL="$2"
      shift
      ;;
    --skip-doc)
      SKIP_DOC=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 2
      ;;
  esac
  shift
done

if ! command -v adb >/dev/null 2>&1; then
  echo "[FAIL] adb command not found."
  exit 1
fi

if [[ -z "$SERIAL" ]]; then
  PHYSICAL_SERIALS=()
  while IFS= read -r physical_serial; do
    [[ -n "$physical_serial" ]] && PHYSICAL_SERIALS+=("$physical_serial")
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')

  if [[ "${#PHYSICAL_SERIALS[@]}" -eq 0 ]]; then
    echo "[FAIL] No physical device connected. Connect one device and retry."
    exit 1
  fi
  if [[ "${#PHYSICAL_SERIALS[@]}" -gt 1 ]]; then
    echo "[FAIL] Multiple physical devices detected: ${PHYSICAL_SERIALS[*]}"
    echo "Use --serial <adb-serial>."
    exit 1
  fi
  SERIAL="${PHYSICAL_SERIALS[0]}"
fi

if [[ -z "$SERIAL" ]]; then
  echo "[FAIL] Empty serial value. Use --serial <adb-serial>."
  exit 1
fi

if ! adb -s "$SERIAL" get-state >/dev/null 2>&1; then
  echo "[FAIL] Device '$SERIAL' is not available via adb."
  exit 1
fi

MANUFACTURER="$(adb -s "$SERIAL" shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')"
MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
ANDROID_RELEASE="$(adb -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')"
SDK_INT="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
RUN_AT="$(date '+%Y-%m-%d %H:%M:%S %z')"

echo "[INFO] Running connectedDebugAndroidTest on physical device"
echo "       serial=$SERIAL, model=${MANUFACTURER} ${MODEL}, android=${ANDROID_RELEASE} (SDK ${SDK_INT})"

set +e
ANDROID_SERIAL="$SERIAL" ./gradlew :app:connectedDebugAndroidTest
RESULT_CODE=$?
set -e

if [[ "$RESULT_CODE" -eq 0 ]]; then
  RESULT_TEXT="PASS"
  echo "[PASS] connectedDebugAndroidTest finished successfully on $SERIAL"
else
  RESULT_TEXT="FAIL(${RESULT_CODE})"
  echo "[FAIL] connectedDebugAndroidTest failed on $SERIAL"
fi

if [[ "$SKIP_DOC" -eq 0 ]]; then
  REPORT_FILE="docs/18-device-validation-report.md"
  {
    echo
    echo "## ${RUN_AT}"
    echo "- 결과: ${RESULT_TEXT}"
    echo "- 디바이스: ${MANUFACTURER} ${MODEL}"
    echo "- Android: ${ANDROID_RELEASE} (SDK ${SDK_INT})"
    echo "- ADB Serial: \`${SERIAL}\`"
    echo "- 실행 명령: \`ANDROID_SERIAL=${SERIAL} ./gradlew :app:connectedDebugAndroidTest\`"
  } >> "$REPORT_FILE"
  echo "[INFO] Report updated: $REPORT_FILE"
fi

exit "$RESULT_CODE"
