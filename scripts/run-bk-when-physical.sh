#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

POLL_INTERVAL=10
TIMEOUT_SECONDS=0
DATE_TAG="$(date +%F)"
EMULATOR_REPORT=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-bk-when-physical.sh [options]

Options:
  --poll-interval <seconds>         poll interval for adb device scan (default: 10).
  --timeout-seconds <seconds>       stop waiting after timeout (default: 0: wait forever).
  --date-tag <yyyy-mm-dd>           report date tag.
  --emulator-report <path>          emulator report path.
  -h, --help                        show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --poll-interval)
      POLL_INTERVAL="${2:-}"
      shift 2
      ;;
    --timeout-seconds)
      TIMEOUT_SECONDS="${2:-}"
      shift 2
      ;;
    --date-tag)
      DATE_TAG="${2:-}"
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

if [[ -z "$EMULATOR_REPORT" ]]; then
  if [[ -f "docs/assets/distribution/performance_gate_emulator_s06_${DATE_TAG}.md" ]]; then
    EMULATOR_REPORT="docs/assets/distribution/performance_gate_emulator_s06_${DATE_TAG}.md"
  else
    EMULATOR_REPORT="docs/assets/distribution/performance_gate_emulator_${DATE_TAG}.md"
  fi
fi

find_physical_serial() {
  adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}'
}

echo "[INFO] Waiting for physical device..."
echo "       poll_interval=${POLL_INTERVAL}s timeout=${TIMEOUT_SECONDS}s date_tag=${DATE_TAG}"
echo "       emulator_report=${EMULATOR_REPORT}"

started_at="$(date +%s)"
while true; do
  serial="$(find_physical_serial || true)"
  if [[ -n "${serial:-}" ]]; then
    echo "[INFO] Physical device detected: $serial"
    ./scripts/run-bk-device-gate.sh \
      --serial "$serial" \
      --date-tag "$DATE_TAG" \
      --emulator-report "$EMULATOR_REPORT"
    exit 0
  fi

  if [[ "$TIMEOUT_SECONDS" -gt 0 ]]; then
    now="$(date +%s)"
    elapsed=$((now - started_at))
    if [[ "$elapsed" -ge "$TIMEOUT_SECONDS" ]]; then
      echo "[WARN] Timeout reached without physical device."
      exit 2
    fi
  fi

  sleep "$POLL_INTERVAL"
done
