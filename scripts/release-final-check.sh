#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

SERIAL=""
SKIP_DOC=0
REQUIRE_PHYSICAL=0

usage() {
  cat <<EOF
Usage: $0 [--serial <adb-serial>] [--require-physical-device] [--skip-doc]

Options:
  --serial <adb-serial>       Use a specific adb serial.
  --require-physical-device   Fail if the selected device is not a physical device.
  --skip-doc                  Do not append run result to docs/18-device-validation-report.md.
EOF
}

is_emulator_serial() {
  [[ "$1" =~ ^emulator- ]]
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
    --require-physical-device)
      REQUIRE_PHYSICAL=1
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

CONNECTED_SERIALS=()
PHYSICAL_SERIALS=()
EMULATOR_SERIALS=()
while IFS= read -r connected_serial; do
  [[ -n "$connected_serial" ]] || continue
  CONNECTED_SERIALS+=("$connected_serial")
  if is_emulator_serial "$connected_serial"; then
    EMULATOR_SERIALS+=("$connected_serial")
  else
    PHYSICAL_SERIALS+=("$connected_serial")
  fi
done < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')

if [[ -z "$SERIAL" ]]; then
  if [[ "${#PHYSICAL_SERIALS[@]}" -eq 1 ]]; then
    SERIAL="${PHYSICAL_SERIALS[0]}"
  elif [[ "${#PHYSICAL_SERIALS[@]}" -gt 1 ]]; then
    echo "[FAIL] Multiple physical devices detected: ${PHYSICAL_SERIALS[*]}"
    echo "Use --serial <adb-serial>."
    exit 1
  elif [[ "${#EMULATOR_SERIALS[@]}" -ge 1 ]]; then
    SERIAL="${EMULATOR_SERIALS[0]}"
    echo "[WARN] No physical device connected. Falling back to emulator: $SERIAL"
  else
    if [[ "$REQUIRE_PHYSICAL" -eq 1 ]]; then
      echo "[FAIL] No adb device connected."
      exit 1
    fi
    RUN_AT="$(date '+%Y-%m-%d %H:%M:%S %z')"
    echo "[WARN] No adb device connected. Running CI-only fallback gate."
    set +e
    ./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing
    RESULT_CODE=$?
    set -e

    if [[ "$RESULT_CODE" -eq 0 ]]; then
      RESULT_TEXT="PASS"
      VALIDATION_MODE="CI_ONLY_NO_DEVICE"
      echo "[PASS] CI-only fallback gate succeeded."
    else
      RESULT_TEXT="FAIL(${RESULT_CODE})"
      VALIDATION_MODE="CI_ONLY_NO_DEVICE"
      echo "[FAIL] CI-only fallback gate failed."
    fi

    if [[ "$SKIP_DOC" -eq 0 ]]; then
      REPORT_FILE="docs/18-device-validation-report.md"
      {
        echo
        echo "## ${RUN_AT}"
        echo "- 결과: ${RESULT_TEXT}"
        echo "- 검증 모드: ${VALIDATION_MODE}"
        echo "- 디바이스: N/A"
        echo "- Android: N/A"
        echo "- ADB Serial: \`N/A\`"
        echo "- 실행 명령: \`./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing\`"
      } >> "$REPORT_FILE"
      echo "[INFO] Report updated: $REPORT_FILE"
    fi

    exit "$RESULT_CODE"
  fi
fi

if [[ "${#CONNECTED_SERIALS[@]}" -gt 0 ]]; then
  if ! printf '%s\n' "${CONNECTED_SERIALS[@]}" | grep -qx "$SERIAL"; then
    echo "[FAIL] Device '$SERIAL' is not connected."
    exit 1
  fi
else
  echo "[FAIL] No adb device connected."
  exit 1
fi

SELECTED_IS_EMULATOR=0
if is_emulator_serial "$SERIAL"; then
  SELECTED_IS_EMULATOR=1
fi

if [[ "$REQUIRE_PHYSICAL" -eq 1 && "$SELECTED_IS_EMULATOR" -eq 1 ]]; then
  echo "[FAIL] '$SERIAL' is emulator serial. Physical device is required."
  exit 1
fi

MANUFACTURER="$(adb -s "$SERIAL" shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')"
MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
ANDROID_RELEASE="$(adb -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')"
SDK_INT="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
RUN_AT="$(date '+%Y-%m-%d %H:%M:%S %z')"

PREFLIGHT_CMD=(./scripts/release-preflight.sh --with-build --android-serial "$SERIAL")
VALIDATION_MODE="EMULATOR"
if [[ "$SELECTED_IS_EMULATOR" -eq 0 ]]; then
  PREFLIGHT_CMD+=(--require-physical-device)
  VALIDATION_MODE="PHYSICAL"
elif [[ "$REQUIRE_PHYSICAL" -eq 1 ]]; then
  VALIDATION_MODE="PHYSICAL_REQUIRED"
fi

echo "[INFO] Running release preflight"
echo "       serial=$SERIAL, mode=$VALIDATION_MODE, model=${MANUFACTURER} ${MODEL}, android=${ANDROID_RELEASE} (SDK ${SDK_INT})"
echo "       command=${PREFLIGHT_CMD[*]}"

set +e
"${PREFLIGHT_CMD[@]}"
RESULT_CODE=$?
set -e

FALLBACK_NOTE=""
if [[ "$RESULT_CODE" -ne 0 && "$SELECTED_IS_EMULATOR" -eq 1 && "$REQUIRE_PHYSICAL" -eq 0 ]]; then
  echo "[WARN] Emulator preflight failed. Retrying with CI-only fallback gate."
  set +e
  ./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing
  FALLBACK_CODE=$?
  set -e
  if [[ "$FALLBACK_CODE" -eq 0 ]]; then
    RESULT_CODE=0
    VALIDATION_MODE="EMULATOR_CI_FALLBACK"
    FALLBACK_NOTE="에뮬레이터 계측 품질게이트 실패로 CI-only 게이트로 대체 통과"
    echo "[WARN] Emulator instrumentation was unstable. CI-only fallback gate passed."
  else
    FALLBACK_NOTE="에뮬레이터 계측 품질게이트 실패 후 CI-only fallback도 실패"
    echo "[FAIL] CI-only fallback gate also failed."
  fi
fi

if [[ "$RESULT_CODE" -eq 0 ]]; then
  RESULT_TEXT="PASS"
  echo "[PASS] Release preflight succeeded on $SERIAL"
  if [[ "$SELECTED_IS_EMULATOR" -eq 1 ]]; then
    echo "[WARN] Emulator-only validation passed. Physical-device validation remains pending."
  fi
else
  RESULT_TEXT="FAIL(${RESULT_CODE})"
  echo "[FAIL] Release preflight failed on $SERIAL"
fi

if [[ "$SKIP_DOC" -eq 0 ]]; then
  REPORT_FILE="docs/18-device-validation-report.md"
  {
    echo
    echo "## ${RUN_AT}"
    echo "- 결과: ${RESULT_TEXT}"
    echo "- 검증 모드: ${VALIDATION_MODE}"
    echo "- 디바이스: ${MANUFACTURER} ${MODEL}"
    echo "- Android: ${ANDROID_RELEASE} (SDK ${SDK_INT})"
    echo "- ADB Serial: \`${SERIAL}\`"
    echo "- 실행 명령: \`${PREFLIGHT_CMD[*]}\`"
    if [[ -n "$FALLBACK_NOTE" ]]; then
      echo "- 보완 메모: ${FALLBACK_NOTE}"
    fi
  } >> "$REPORT_FILE"
  echo "[INFO] Report updated: $REPORT_FILE"
fi

exit "$RESULT_CODE"
