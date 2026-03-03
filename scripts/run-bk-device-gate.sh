#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

SERIAL=""
DATE_TAG="$(date +%F)"
EMULATOR_REPORT=""
DEVICE_REPORT=""
DECISION_REPORT=""
SAVE_BLOCKED_REPORT=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-bk-device-gate.sh [options]

Options:
  --serial <adb-serial>              physical device serial.
  --date-tag <yyyy-mm-dd>            report date tag.
  --emulator-report <path>           emulator report path (default: latest s06/emulator report).
  --device-report <path>             output device report path.
  --decision-report <path>           output decision report path.
  --save-blocked-report              write pending decision report when physical device is missing.
  -h, --help                         show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
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
    --device-report)
      DEVICE_REPORT="${2:-}"
      shift 2
      ;;
    --decision-report)
      DECISION_REPORT="${2:-}"
      shift 2
      ;;
    --save-blocked-report)
      SAVE_BLOCKED_REPORT=1
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

if [[ -z "$EMULATOR_REPORT" ]]; then
  if [[ -f "docs/assets/distribution/performance_gate_emulator_s06_${DATE_TAG}.md" ]]; then
    EMULATOR_REPORT="docs/assets/distribution/performance_gate_emulator_s06_${DATE_TAG}.md"
  elif [[ -f "docs/assets/distribution/performance_gate_emulator_${DATE_TAG}.md" ]]; then
    EMULATOR_REPORT="docs/assets/distribution/performance_gate_emulator_${DATE_TAG}.md"
  else
    EMULATOR_REPORT="$(ls -1t docs/assets/distribution/performance_gate_emulator*.md 2>/dev/null | head -n 1 || true)"
  fi
fi

if [[ -z "$DEVICE_REPORT" ]]; then
  DEVICE_REPORT="docs/assets/distribution/performance_gate_device_${DATE_TAG}.md"
fi

if [[ -z "$DECISION_REPORT" ]]; then
  DECISION_REPORT="docs/assets/distribution/performance_release_decision_${DATE_TAG}.md"
fi

discover_physical_serial() {
  adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}'
}

if [[ -z "$SERIAL" ]]; then
  SERIAL="$(discover_physical_serial || true)"
fi

if [[ -z "$SERIAL" ]]; then
  echo "[WARN] Physical device not found. BK-001/BK-002 remains blocked."
  if [[ "$SAVE_BLOCKED_REPORT" -eq 1 ]]; then
    mkdir -p "$(dirname "$DECISION_REPORT")"
    cat > "$DECISION_REPORT" <<EOF
# 성능 릴리즈 판정 리포트

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- Emulator report: ${EMULATOR_REPORT:-N/A}
- Device report: N/A (no physical device)
- Decision: PENDING_DEVICE_VALIDATION
- Release Status: PENDING
- Follow-up: physical device 연결 후 BK-001/BK-002 재실행 필요
EOF
    echo "[INFO] Saved blocked decision report: $DECISION_REPORT"
  fi
  exit 2
fi

echo "[INFO] Run BK-001 device profile report (serial=$SERIAL)"
./scripts/run-performance-sample-check.sh \
  --serial "$SERIAL" \
  --profile device \
  --repeat 5 \
  --warmup 1 \
  --save-report "$DEVICE_REPORT"

echo "[INFO] Run BK-002 release decision"
./scripts/evaluate-performance-gate.sh \
  --emulator-report "$EMULATOR_REPORT" \
  --device-report "$DEVICE_REPORT" \
  --save-report "$DECISION_REPORT"

echo "[PASS] BK device gate completed."
echo "       device_report=$DEVICE_REPORT"
echo "       decision_report=$DECISION_REPORT"
