#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# Keep Gradle caches inside the workspace to avoid home-partition disk pressure.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

SERIAL=""
PROFILE="emulator"
REPEAT=5
WARMUP=1
SAVE_REPORT=""
BASELINE_REPORT=""
PACKAGE_NAME="com.weeklylotto.app.debug"
ACTIVITY_NAME="com.weeklylotto.app.MainActivity"

DEVICE_STARTUP_P95_MS=2200
DEVICE_JANK_P95_PERCENT=3.0
DEVICE_ANR_MAX=0

EMULATOR_STARTUP_DEGRADE_WARN_PERCENT=15
EMULATOR_STARTUP_DEGRADE_FAIL_PERCENT=30
EMULATOR_JANK_DEGRADE_WARN_PERCENT=20
EMULATOR_JANK_DEGRADE_FAIL_PERCENT=40

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-performance-sample-check.sh [options]

Options:
  --serial <adb-serial>         adb device serial to target.
  --profile <emulator|device>   measurement profile (default: emulator).
  --repeat <count>              measured runs count, warm-up excluded (default: 5).
  --warmup <count>              warm-up runs count (default: 1).
  --save-report <path>          save markdown report to file.
  --baseline-report <path>      baseline report for emulator profile comparison.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --profile)
      PROFILE="${2:-}"
      shift 2
      ;;
    --repeat)
      REPEAT="${2:-}"
      shift 2
      ;;
    --warmup)
      WARMUP="${2:-}"
      shift 2
      ;;
    --save-report)
      SAVE_REPORT="${2:-}"
      shift 2
      ;;
    --baseline-report)
      BASELINE_REPORT="${2:-}"
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

if [[ "$PROFILE" != "emulator" && "$PROFILE" != "device" ]]; then
  echo "[FAIL] Invalid --profile value: $PROFILE (expected emulator|device)"
  exit 1
fi

if ! [[ "$REPEAT" =~ ^[0-9]+$ ]] || [[ "$REPEAT" -lt 1 ]]; then
  echo "[FAIL] --repeat must be a positive integer."
  exit 1
fi

if ! [[ "$WARMUP" =~ ^[0-9]+$ ]]; then
  echo "[FAIL] --warmup must be zero or a positive integer."
  exit 1
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "[FAIL] adb command not found."
  exit 1
fi

if [[ -z "$SERIAL" ]]; then
  SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
fi

if [[ -z "$SERIAL" ]]; then
  echo "[FAIL] No connected adb device found. Use --serial <adb-serial>."
  exit 1
fi

if ! adb -s "$SERIAL" get-state >/dev/null 2>&1; then
  echo "[FAIL] Device '$SERIAL' is not connected."
  exit 1
fi

TOTAL_RUNS=$((REPEAT + WARMUP))

if [[ "$TOTAL_RUNS" -lt 1 ]]; then
  echo "[FAIL] Invalid run configuration: repeat + warmup must be at least 1."
  exit 1
fi

float_gt() {
  awk -v lhs="$1" -v rhs="$2" 'BEGIN { exit !(lhs > rhs) }'
}

float_ge() {
  awk -v lhs="$1" -v rhs="$2" 'BEGIN { exit !(lhs >= rhs) }'
}

float_eq() {
  awk -v lhs="$1" -v rhs="$2" 'BEGIN { exit !(lhs == rhs) }'
}

extract_startup_ms() {
  local output="$1"
  local value
  value="$(printf "%s\n" "$output" | awk -F ':' '/TotalTime/ {gsub(/[[:space:]]/, "", $2); print $2; exit}')"
  if [[ -z "$value" ]]; then
    value="$(printf "%s\n" "$output" | awk -F ':' '/ThisTime/ {gsub(/[[:space:]]/, "", $2); print $2; exit}')"
  fi
  echo "${value:-0}"
}

extract_jank_percent() {
  local output="$1"
  local line
  line="$(printf "%s\n" "$output" | awk '/Janky frames:/ {print; exit}')"
  if [[ -z "$line" ]]; then
    echo "0.00"
    return
  fi
  printf "%s\n" "$line" | sed -E 's/.*\(([0-9]+(\.[0-9]+)?)%\).*/\1/'
}

anr_count_for_package() {
  local output
  output="$(adb -s "$SERIAL" shell dumpsys activity exit-info "$PACKAGE_NAME" 2>/dev/null | tr -d '\r' || true)"
  printf "%s\n" "$output" | awk 'BEGIN {IGNORECASE=1} /ANR/ {count++} END {print count+0}'
}

calc_median() {
  printf "%s\n" "$@" | sort -n | awk '
    { a[NR] = $1 }
    END {
      if (NR == 0) { printf "0.00"; exit }
      if (NR % 2 == 1) {
        printf "%.2f", a[(NR + 1) / 2]
      } else {
        printf "%.2f", (a[NR / 2] + a[(NR / 2) + 1]) / 2
      }
    }'
}

calc_percentile() {
  local percentile="$1"
  shift
  printf "%s\n" "$@" | sort -n | awk -v p="$percentile" '
    { a[NR] = $1 }
    END {
      if (NR == 0) { printf "0.00"; exit }
      idx = int((p * NR) + 0.999999)
      if (idx < 1) idx = 1
      if (idx > NR) idx = NR
      printf "%.2f", a[idx]
    }'
}

calc_degradation_percent() {
  local current="$1"
  local baseline="$2"
  awk -v cur="$current" -v base="$baseline" '
    BEGIN {
      if (base == 0) {
        printf "0.00"
      } else {
        printf "%.2f", ((cur - base) / base) * 100.0
      }
    }'
}

discover_emulator_baseline_report() {
  local candidate=""
  while IFS= read -r path; do
    [[ -n "$path" ]] || continue
    if [[ -n "$SAVE_REPORT" && "$path" == "$SAVE_REPORT" ]]; then
      continue
    fi
    candidate="$path"
    break
  done < <(ls -1t docs/assets/distribution/performance_gate_emulator_*.md 2>/dev/null || true)

  if [[ -n "$candidate" ]]; then
    echo "$candidate"
    return
  fi

  if [[ -f "docs/assets/distribution/performance_sample_2026-02-26.md" ]]; then
    echo "docs/assets/distribution/performance_sample_2026-02-26.md"
  fi
}

extract_baseline_metric() {
  local file="$1"
  local key="$2"
  local value=""
  case "$key" in
    startup_p95)
      value="$(grep -Eo 'Startup (P95|TotalTime): [0-9]+([.][0-9]+)?ms' "$file" | head -n 1 | sed -E 's/.*: ([0-9]+([.][0-9]+)?)ms/\1/' || true)"
      ;;
    jank_p95)
      value="$(grep -Eo '(Jank P95|Janky frames): [0-9]+([.][0-9]+)?%' "$file" | head -n 1 | sed -E 's/.*: ([0-9]+([.][0-9]+)?)%/\1/' || true)"
      ;;
  esac
  echo "$value"
}

DEVICE_MANUFACTURER="$(adb -s "$SERIAL" shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')"
DEVICE_MODEL="$(adb -s "$SERIAL" shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
DEVICE_OS="$(adb -s "$SERIAL" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')"
DEVICE_API="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
IS_EMULATOR_SERIAL=0
if [[ "$SERIAL" =~ ^emulator- ]]; then
  IS_EMULATOR_SERIAL=1
fi
DEVICE_CLASS="physical"
if [[ "$IS_EMULATOR_SERIAL" -eq 1 ]]; then
  DEVICE_CLASS="emulator"
fi

if [[ "$PROFILE" == "device" && "$IS_EMULATOR_SERIAL" -eq 1 ]]; then
  echo "[WARN] profile=device로 실행했지만 대상 serial은 에뮬레이터입니다: $SERIAL"
fi

if [[ "$PROFILE" == "emulator" && -z "$BASELINE_REPORT" ]]; then
  BASELINE_REPORT="$(discover_emulator_baseline_report || true)"
fi

echo "[INFO] Running performance sample check"
echo "       profile=$PROFILE serial=$SERIAL runs=$REPEAT warmup=$WARMUP total=$TOTAL_RUNS"

declare -a STARTUP_SAMPLES=()
declare -a JANK_SAMPLES=()
declare -a STARTUP_WARMUP=()
declare -a JANK_WARMUP=()
declare -a RAW_START_OUTPUTS=()
declare -a RAW_JANK_OUTPUTS=()

APP_INSTALL_ATTEMPTED=0
ANR_BEFORE="$(anr_count_for_package)"
ANR_AFTER="$ANR_BEFORE"

for ((run=1; run<=TOTAL_RUNS; run++)); do
  adb -s "$SERIAL" shell am force-stop "$PACKAGE_NAME" >/dev/null 2>&1 || true
  adb -s "$SERIAL" shell dumpsys gfxinfo "$PACKAGE_NAME" reset >/dev/null 2>&1 || true

  START_OUTPUT="$( (adb -s "$SERIAL" shell am start -W -n "$PACKAGE_NAME/$ACTIVITY_NAME" 2>/dev/null || true) | tr -d '\r')"
  if printf "%s\n" "$START_OUTPUT" | grep -Eq "Error type 3|does not exist"; then
    if [[ "$APP_INSTALL_ATTEMPTED" -eq 0 ]]; then
      echo "[WARN] Debug app/activity not found. Installing debug APK and retrying."
      ANDROID_SERIAL="$SERIAL" ./gradlew :app:installDebug >/dev/null
      APP_INSTALL_ATTEMPTED=1
      START_OUTPUT="$( (adb -s "$SERIAL" shell am start -W -n "$PACKAGE_NAME/$ACTIVITY_NAME" 2>/dev/null || true) | tr -d '\r')"
    fi
  fi

  if printf "%s\n" "$START_OUTPUT" | grep -Eq "Error type 3|does not exist|Error:"; then
    echo "[FAIL] Failed to launch app activity for performance sampling."
    echo "$START_OUTPUT"
    exit 1
  fi

  STARTUP_MS="$(extract_startup_ms "$START_OUTPUT")"
  sleep 1
  GFX_OUTPUT="$(adb -s "$SERIAL" shell dumpsys gfxinfo "$PACKAGE_NAME" 2>/dev/null | tr -d '\r')"
  JANK_PERCENT="$(extract_jank_percent "$GFX_OUTPUT")"

  RAW_START_OUTPUTS+=("$START_OUTPUT")
  RAW_JANK_OUTPUTS+=("$GFX_OUTPUT")

  if [[ "$run" -le "$WARMUP" ]]; then
    STARTUP_WARMUP+=("$STARTUP_MS")
    JANK_WARMUP+=("$JANK_PERCENT")
    echo "[INFO] warm-up #$run startup=${STARTUP_MS}ms jank=${JANK_PERCENT}%"
  else
    SAMPLE_INDEX=$((run - WARMUP))
    STARTUP_SAMPLES+=("$STARTUP_MS")
    JANK_SAMPLES+=("$JANK_PERCENT")
    echo "[INFO] sample #$SAMPLE_INDEX startup=${STARTUP_MS}ms jank=${JANK_PERCENT}%"
  fi

  ANR_AFTER="$(anr_count_for_package)"
done

ANR_DELTA=$((ANR_AFTER - ANR_BEFORE))
if [[ "$ANR_DELTA" -lt 0 ]]; then
  ANR_DELTA=0
fi

if [[ "${#STARTUP_SAMPLES[@]}" -eq 0 ]]; then
  echo "[FAIL] No measured samples collected after warm-up."
  exit 1
fi

STARTUP_MEDIAN="$(calc_median "${STARTUP_SAMPLES[@]}")"
STARTUP_P95="$(calc_percentile 0.95 "${STARTUP_SAMPLES[@]}")"
JANK_MEDIAN="$(calc_median "${JANK_SAMPLES[@]}")"
JANK_P95="$(calc_percentile 0.95 "${JANK_SAMPLES[@]}")"

STARTUP_STATUS="PASS"
JANK_STATUS="PASS"
ANR_STATUS="PASS"
FINAL_VERDICT="PASS"
GATE_DECISION="PROCEED"
RELEASE_BLOCKING="NO"
BASELINE_NOTE="N/A"
BASELINE_STARTUP_P95=""
BASELINE_JANK_P95=""
STARTUP_DEGRADATION="0.00"
JANK_DEGRADATION="0.00"

if [[ "$PROFILE" == "device" ]]; then
  if float_gt "$STARTUP_P95" "$DEVICE_STARTUP_P95_MS"; then
    STARTUP_STATUS="FAIL"
  fi
  if float_gt "$JANK_P95" "$DEVICE_JANK_P95_PERCENT"; then
    JANK_STATUS="FAIL"
  fi
  if [[ "$ANR_DELTA" -gt "$DEVICE_ANR_MAX" ]]; then
    ANR_STATUS="FAIL"
  fi

  if [[ "$STARTUP_STATUS" == "FAIL" || "$JANK_STATUS" == "FAIL" || "$ANR_STATUS" == "FAIL" ]]; then
    FINAL_VERDICT="FAIL"
    GATE_DECISION="HOLD"
    RELEASE_BLOCKING="YES"
  fi
else
  if [[ "$ANR_DELTA" -gt "$DEVICE_ANR_MAX" ]]; then
    ANR_STATUS="FAIL"
  fi

  if [[ -n "$BASELINE_REPORT" && -f "$BASELINE_REPORT" ]]; then
    BASELINE_STARTUP_P95="$(extract_baseline_metric "$BASELINE_REPORT" startup_p95)"
    BASELINE_JANK_P95="$(extract_baseline_metric "$BASELINE_REPORT" jank_p95)"
  fi

  if [[ -n "$BASELINE_STARTUP_P95" && -n "$BASELINE_JANK_P95" ]]; then
    STARTUP_DEGRADATION="$(calc_degradation_percent "$STARTUP_P95" "$BASELINE_STARTUP_P95")"
    JANK_DEGRADATION="$(calc_degradation_percent "$JANK_P95" "$BASELINE_JANK_P95")"
    BASELINE_NOTE="$(basename "$BASELINE_REPORT")"

    if float_ge "$STARTUP_DEGRADATION" "$EMULATOR_STARTUP_DEGRADE_FAIL_PERCENT"; then
      STARTUP_STATUS="FAIL"
    elif float_ge "$STARTUP_DEGRADATION" "$EMULATOR_STARTUP_DEGRADE_WARN_PERCENT"; then
      STARTUP_STATUS="WARN"
    fi

    if float_ge "$JANK_DEGRADATION" "$EMULATOR_JANK_DEGRADE_FAIL_PERCENT"; then
      JANK_STATUS="FAIL"
    elif float_ge "$JANK_DEGRADATION" "$EMULATOR_JANK_DEGRADE_WARN_PERCENT"; then
      JANK_STATUS="WARN"
    fi
  else
    STARTUP_STATUS="WARN"
    JANK_STATUS="WARN"
    BASELINE_NOTE="baseline_missing"
  fi

  if [[ "$ANR_STATUS" == "FAIL" || "$STARTUP_STATUS" == "FAIL" || "$JANK_STATUS" == "FAIL" ]]; then
    FINAL_VERDICT="FAIL"
  elif [[ "$STARTUP_STATUS" == "WARN" || "$JANK_STATUS" == "WARN" ]]; then
    FINAL_VERDICT="WARN"
  fi

  GATE_DECISION="PROCEED"
  if [[ "$FINAL_VERDICT" == "FAIL" || "$FINAL_VERDICT" == "WARN" ]]; then
    GATE_DECISION="PROCEED_WITH_OPTIMIZATION_BACKLOG"
  fi
fi

echo "[INFO] Startup median=${STARTUP_MEDIAN}ms, P95=${STARTUP_P95}ms => ${STARTUP_STATUS}"
echo "[INFO] Jank median=${JANK_MEDIAN}%, P95=${JANK_P95}% => ${JANK_STATUS}"
echo "[INFO] ANR delta=${ANR_DELTA} => ${ANR_STATUS}"
echo "[INFO] Final verdict=${FINAL_VERDICT}, gate_decision=${GATE_DECISION}, release_blocking=${RELEASE_BLOCKING}"

if [[ "$PROFILE" == "emulator" ]]; then
  echo "[INFO] Baseline source=${BASELINE_NOTE} startup_degradation=${STARTUP_DEGRADATION}% jank_degradation=${JANK_DEGRADATION}%"
fi

if [[ -n "$SAVE_REPORT" ]]; then
  mkdir -p "$(dirname "$SAVE_REPORT")"
  {
    echo "# 성능 게이트 리포트"
    echo
    echo "- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')"
    echo "- Profile: ${PROFILE}"
    echo "- Device Class: ${DEVICE_CLASS}"
    echo "- Device: ${DEVICE_MANUFACTURER} ${DEVICE_MODEL} (${SERIAL})"
    echo "- OS/API: Android ${DEVICE_OS} (API ${DEVICE_API})"
    echo "- Package: \`${PACKAGE_NAME}\`"
    echo "- Runs: total=${TOTAL_RUNS}, warmup=${WARMUP}, measured=${REPEAT}"
    echo
    echo "## 집계"
    echo "- Startup median: ${STARTUP_MEDIAN}ms"
    echo "- Startup P95: ${STARTUP_P95}ms"
    echo "- Jank median: ${JANK_MEDIAN}%"
    echo "- Jank P95: ${JANK_P95}%"
    echo "- ANR count(delta): ${ANR_DELTA}"
    if [[ "$PROFILE" == "device" ]]; then
      echo "- Baseline 비교: N/A (device 절대 임계치 사용)"
    else
      echo "- Baseline source: ${BASELINE_NOTE}"
      if [[ -n "$BASELINE_STARTUP_P95" && -n "$BASELINE_JANK_P95" ]]; then
        echo "- Baseline Startup P95: ${BASELINE_STARTUP_P95}ms"
        echo "- Baseline Jank P95: ${BASELINE_JANK_P95}%"
        echo "- Startup degradation: ${STARTUP_DEGRADATION}%"
        echo "- Jank degradation: ${JANK_DEGRADATION}%"
      else
        echo "- Baseline Startup P95: N/A"
        echo "- Baseline Jank P95: N/A"
      fi
    fi
    echo
    echo "## 판정"
    echo "- Startup Status: ${STARTUP_STATUS}"
    echo "- Jank Status: ${JANK_STATUS}"
    echo "- ANR Status: ${ANR_STATUS}"
    echo "- Final Verdict: ${FINAL_VERDICT}"
    echo "- Gate Decision: ${GATE_DECISION}"
    echo "- Release Blocking: ${RELEASE_BLOCKING}"
    echo
    echo "## 샘플"
    echo "| Index | Startup(ms) | Jank(%) | Segment |"
    echo "|---|---:|---:|---|"
    for i in "${!STARTUP_WARMUP[@]}"; do
      idx=$((i + 1))
      echo "| ${idx} | ${STARTUP_WARMUP[$i]} | ${JANK_WARMUP[$i]} | warmup |"
    done
    for i in "${!STARTUP_SAMPLES[@]}"; do
      idx=$((i + 1))
      echo "| ${idx} | ${STARTUP_SAMPLES[$i]} | ${JANK_SAMPLES[$i]} | measured |"
    done
    echo
    echo "## Notes"
    if [[ "$PROFILE" == "device" && "$IS_EMULATOR_SERIAL" -eq 1 ]]; then
      echo "- profile=device 실행이지만 serial은 에뮬레이터입니다. 실기기 재검증이 필요합니다."
    fi
    if [[ "$PROFILE" == "emulator" && "$BASELINE_NOTE" == "baseline_missing" ]]; then
      echo "- baseline 리포트가 없어 WARN으로 처리되었습니다."
    fi
  } > "$SAVE_REPORT"
  echo "[INFO] Saved report: $SAVE_REPORT"
fi

if [[ "$FINAL_VERDICT" == "FAIL" ]]; then
  echo "[FAIL] Performance sample gate failed."
  exit 1
fi

if [[ "$FINAL_VERDICT" == "WARN" ]]; then
  echo "[WARN] Performance sample gate finished with WARN."
else
  echo "[PASS] Performance sample gate passed."
fi

exit 0
