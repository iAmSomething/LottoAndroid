#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
TIMEOUT_SECONDS=0
POLL_INTERVAL=5
SAVE_BLOCKED_REPORT=0
SKIP_INSTALL=0
REPORT_PATH=""

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-p4-when-wear-physical.sh [options]

Options:
  --date-tag <yyyy-mm-dd>         report date tag.
  --timeout-seconds <sec>         wait timeout. 0 means infinite.
  --poll-interval <sec>           polling interval (default: 5).
  --report-path <path>            output report path.
  --save-blocked-report           save blocked report on timeout/no-device.
  --skip-install                  pass --skip-install to run-p4-wear-proof-gate.sh.
  -h, --help                      show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --timeout-seconds)
      TIMEOUT_SECONDS="${2:-0}"
      shift 2
      ;;
    --poll-interval)
      POLL_INTERVAL="${2:-5}"
      shift 2
      ;;
    --report-path)
      REPORT_PATH="${2:-}"
      shift 2
      ;;
    --save-blocked-report)
      SAVE_BLOCKED_REPORT=1
      shift 1
      ;;
    --skip-install)
      SKIP_INSTALL=1
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

if ! command -v adb >/dev/null 2>&1; then
  echo "[FAIL] adb command not found."
  exit 1
fi

if [[ -z "$REPORT_PATH" ]]; then
  REPORT_PATH="docs/assets/distribution/wear_p4_device_evidence_${DATE_TAG}.md"
fi

discover_small_large() {
  local ranked=""
  local serial characteristics size area
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    characteristics="$(adb -s "$serial" shell getprop ro.build.characteristics 2>/dev/null | tr -d '\r' || true)"
    [[ "$characteristics" != *watch* ]] && continue
    size="$(adb -s "$serial" shell wm size 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical size/ {print $2; exit}')"
    area=0
    if [[ "$size" =~ ^([0-9]+)x([0-9]+)$ ]]; then
      area="$(( ${BASH_REMATCH[1]} * ${BASH_REMATCH[2]} ))"
    fi
    ranked+="${area} ${serial}"$'\n'
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')

  if [[ -z "$ranked" ]]; then
    return 1
  fi

  local sorted
  sorted="$(printf '%s' "$ranked" | sed '/^$/d' | sort -n)"
  local count
  count="$(printf '%s\n' "$sorted" | sed '/^$/d' | wc -l | tr -d ' ')"
  if [[ "$count" -lt 2 ]]; then
    return 1
  fi

  local small large
  small="$(printf '%s\n' "$sorted" | awk 'NR==1 {print $2}')"
  large="$(printf '%s\n' "$sorted" | awk 'END {print $2}')"
  if [[ -z "$small" || -z "$large" || "$small" == "$large" ]]; then
    return 1
  fi
  echo "$small $large"
}

START_TIME="$(date +%s)"
echo "[INFO] Waiting for two physical wear devices (small + large)..."

while true; do
  if pair="$(discover_small_large)"; then
    small_serial="$(printf '%s' "$pair" | awk '{print $1}')"
    large_serial="$(printf '%s' "$pair" | awk '{print $2}')"
    echo "[INFO] Detected wear devices: small=$small_serial, large=$large_serial"
    cmd=(./scripts/run-p4-wear-proof-gate.sh --small-serial "$small_serial" --large-serial "$large_serial" --date-tag "$DATE_TAG" --report-path "$REPORT_PATH")
    if [[ "$SKIP_INSTALL" -eq 1 ]]; then
      cmd+=(--skip-install)
    fi
    "${cmd[@]}"
    exit $?
  fi

  if [[ "$TIMEOUT_SECONDS" -gt 0 ]]; then
    now="$(date +%s)"
    elapsed="$((now - START_TIME))"
    if [[ "$elapsed" -ge "$TIMEOUT_SECONDS" ]]; then
      echo "[WARN] Timeout reached while waiting wear physical devices."
      if [[ "$SAVE_BLOCKED_REPORT" -eq 1 ]]; then
        blocked_cmd=(./scripts/run-p4-wear-proof-gate.sh --date-tag "$DATE_TAG" --report-path "$REPORT_PATH" --save-blocked-report)
        if [[ "$SKIP_INSTALL" -eq 1 ]]; then
          blocked_cmd+=(--skip-install)
        fi
        "${blocked_cmd[@]}" || true
      fi
      exit 2
    fi
  fi

  sleep "$POLL_INTERVAL"
done
