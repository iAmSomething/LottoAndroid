#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

SMALL_SERIAL=""
LARGE_SERIAL=""
DATE_TAG="$(date +%F)"
REPORT_PATH=""
SCREENSHOT_DIR=""
LOG_DIR=""
INSTALL_DEBUG=1
SAVE_BLOCKED_REPORT=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-p4-wear-proof-gate.sh [options]

Options:
  --small-serial <adb-serial>        small watch serial.
  --large-serial <adb-serial>        large watch serial.
  --date-tag <yyyy-mm-dd>            report date tag.
  --report-path <path>               output report path.
  --screenshot-dir <dir>             screenshot output directory.
  --log-dir <dir>                    log output directory.
  --skip-install                      skip :wear:installDebug step.
  --save-blocked-report              write blocked report when two physical watches are missing.
  -h, --help                         show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --small-serial)
      SMALL_SERIAL="${2:-}"
      shift 2
      ;;
    --large-serial)
      LARGE_SERIAL="${2:-}"
      shift 2
      ;;
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --report-path)
      REPORT_PATH="${2:-}"
      shift 2
      ;;
    --screenshot-dir)
      SCREENSHOT_DIR="${2:-}"
      shift 2
      ;;
    --log-dir)
      LOG_DIR="${2:-}"
      shift 2
      ;;
    --skip-install)
      INSTALL_DEBUG=0
      shift 1
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

if [[ -z "$REPORT_PATH" ]]; then
  REPORT_PATH="docs/assets/distribution/wear_p4_device_evidence_${DATE_TAG}.md"
fi

if [[ -z "$SCREENSHOT_DIR" ]]; then
  SCREENSHOT_DIR="docs/assets/distribution/wear_p4_${DATE_TAG}/screenshots"
fi

if [[ -z "$LOG_DIR" ]]; then
  LOG_DIR="docs/assets/distribution/wear_p4_${DATE_TAG}/logs"
fi

mkdir -p "$(dirname "$REPORT_PATH")" "$SCREENSHOT_DIR" "$LOG_DIR"

list_physical_wear_devices() {
  local serial
  while IFS= read -r serial; do
    [[ -z "$serial" ]] && continue
    local characteristics
    characteristics="$(adb -s "$serial" shell getprop ro.build.characteristics 2>/dev/null | tr -d '\r' || true)"
    if [[ "$characteristics" == *watch* ]]; then
      echo "$serial"
    fi
  done < <(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')
}

get_display_area() {
  local serial="$1"
  local size
  size="$(adb -s "$serial" shell wm size 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical size/ {print $2; exit}')"
  if [[ "$size" =~ ^([0-9]+)x([0-9]+)$ ]]; then
    echo "$(( ${BASH_REMATCH[1]} * ${BASH_REMATCH[2]} ))"
  else
    echo "0"
  fi
}

write_blocked_report() {
  local reason="$1"
  cat > "$REPORT_PATH" <<EOF
# Wear P-004 실기기 증적 리포트

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- 상태: BLOCKED
- 사유: ${reason}
- 요구 조건: 소형/대형 Wear 실기기 2대 연결
- 후속 실행:
  - \`./scripts/run-p4-wear-proof-gate.sh --save-blocked-report --date-tag ${DATE_TAG}\`
  - \`./scripts/run-p4-when-wear-physical.sh --date-tag ${DATE_TAG}\`
EOF
  echo "[INFO] Saved blocked report: $REPORT_PATH"
}

if [[ -z "$SMALL_SERIAL" || -z "$LARGE_SERIAL" ]]; then
  WEAR_SERIALS=()
  while IFS= read -r wear_serial; do
    [[ -n "$wear_serial" ]] && WEAR_SERIALS+=("$wear_serial")
  done < <(list_physical_wear_devices)
  if [[ "${#WEAR_SERIALS[@]}" -ge 2 ]]; then
    local_ranked="$(
      for serial in "${WEAR_SERIALS[@]}"; do
        printf "%s %s\n" "$(get_display_area "$serial")" "$serial"
      done | sort -n
    )"
    if [[ -z "$SMALL_SERIAL" ]]; then
      SMALL_SERIAL="$(printf '%s\n' "$local_ranked" | awk 'NR==1 {print $2}')"
    fi
    if [[ -z "$LARGE_SERIAL" ]]; then
      LARGE_SERIAL="$(printf '%s\n' "$local_ranked" | awk 'END {print $2}')"
    fi
  fi
fi

if [[ -z "$SMALL_SERIAL" || -z "$LARGE_SERIAL" ]]; then
  echo "[WARN] Two physical wear devices were not found."
  if [[ "$SAVE_BLOCKED_REPORT" -eq 1 ]]; then
    write_blocked_report "physical_wear_device_count_lt_2"
  fi
  exit 2
fi

if [[ "$SMALL_SERIAL" == "$LARGE_SERIAL" ]]; then
  echo "[FAIL] small and large serial cannot be identical: $SMALL_SERIAL"
  if [[ "$SAVE_BLOCKED_REPORT" -eq 1 ]]; then
    write_blocked_report "same_serial_assigned_for_small_large"
  fi
  exit 1
fi

for serial in "$SMALL_SERIAL" "$LARGE_SERIAL"; do
  if ! adb -s "$serial" get-state >/dev/null 2>&1; then
    echo "[FAIL] adb serial not available: $serial"
    exit 1
  fi
done

HAS_FAIL=0
SUMMARY_ROWS=()

collect_device_evidence() {
  local label="$1"
  local serial="$2"
  local manufacturer model release sdk characteristics size density
  manufacturer="$(adb -s "$serial" shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')"
  model="$(adb -s "$serial" shell getprop ro.product.model 2>/dev/null | tr -d '\r')"
  release="$(adb -s "$serial" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r')"
  sdk="$(adb -s "$serial" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
  characteristics="$(adb -s "$serial" shell getprop ro.build.characteristics 2>/dev/null | tr -d '\r')"
  size="$(adb -s "$serial" shell wm size 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical size/ {print $2; exit}')"
  density="$(adb -s "$serial" shell wm density 2>/dev/null | tr -d '\r' | awk -F': ' '/Physical density/ {print $2; exit}')"

  local install_result="SKIPPED"
  if [[ "$INSTALL_DEBUG" -eq 1 ]]; then
    if ANDROID_SERIAL="$serial" ./gradlew :wear:installDebug >/tmp/p4_wear_install_"$serial".log 2>&1; then
      install_result="PASS"
    else
      install_result="FAIL"
      HAS_FAIL=1
    fi
  fi

  adb -s "$serial" logcat -c >/dev/null 2>&1 || true

  local launch_output launch_result total_time first_render_result
  launch_result="PASS"
  launch_output="$(adb -s "$serial" shell am start -W -n com.weeklylotto.wear.debug/com.weeklylotto.wear.MainActivity 2>&1 || true)"
  if [[ "$launch_output" == *"Error type 3"* ]]; then
    launch_output="$(adb -s "$serial" shell am start -W -n com.weeklylotto.wear/com.weeklylotto.wear.MainActivity 2>&1 || true)"
  fi
  if [[ "$launch_output" == *"Error"* || "$launch_output" == *"Exception"* ]]; then
    launch_result="FAIL"
    HAS_FAIL=1
  fi

  total_time="$(printf '%s\n' "$launch_output" | awk -F': ' '/TotalTime/ {print $2; exit}')"
  first_render_result="UNKNOWN"
  if [[ "$total_time" =~ ^[0-9]+$ ]]; then
    if [[ "$total_time" -le 1500 ]]; then
      first_render_result="PASS"
    else
      first_render_result="WARN"
    fi
  fi

  sleep 1

  local scroll_result touch_result
  scroll_result="PASS"
  touch_result="PASS"
  if ! adb -s "$serial" shell input swipe 240 240 240 80 250 >/dev/null 2>&1; then
    scroll_result="FAIL"
    HAS_FAIL=1
  fi
  sleep 1
  if ! adb -s "$serial" shell input tap 200 200 >/dev/null 2>&1; then
    touch_result="FAIL"
    HAS_FAIL=1
  fi

  local screenshot_path log_path
  screenshot_path="${SCREENSHOT_DIR}/${label}_${serial}_home.png"
  log_path="${LOG_DIR}/${label}_${serial}.log"
  if ! adb -s "$serial" exec-out screencap -p > "$screenshot_path"; then
    HAS_FAIL=1
    screenshot_path="${screenshot_path} (capture_fail)"
  fi
  if ! adb -s "$serial" logcat -d -t 250 > "$log_path" 2>/dev/null; then
    if ! adb -s "$serial" logcat -d > "$log_path" 2>/dev/null; then
      HAS_FAIL=1
      log_path="${log_path} (capture_fail)"
    fi
  fi

  SUMMARY_ROWS+=("| ${label} | ${serial} | ${manufacturer} ${model} | ${size:-N/A} | ${density:-N/A} | ${total_time:-N/A} | ${first_render_result} | ${scroll_result} | ${touch_result} |")

  {
    echo
    echo "## ${label} 디바이스"
    echo "- serial: \`${serial}\`"
    echo "- model: ${manufacturer} ${model}"
    echo "- android: ${release} (SDK ${sdk})"
    echo "- characteristics: ${characteristics}"
    echo "- size/density: ${size:-N/A} / ${density:-N/A}"
    echo "- install_debug: ${install_result}"
    echo "- launch: ${launch_result}"
    echo "- first_render_total_time_ms: ${total_time:-N/A} (${first_render_result})"
    echo "- scroll_probe: ${scroll_result}"
    echo "- touch_probe: ${touch_result}"
    echo "- screenshot: \`${screenshot_path}\`"
    echo "- log: \`${log_path}\`"
  } >> "$REPORT_PATH"
}

cat > "$REPORT_PATH" <<EOF
# Wear P-004 실기기 증적 리포트

- 실행 시각: $(date '+%Y-%m-%d %H:%M:%S %z')
- small serial: \`${SMALL_SERIAL}\`
- large serial: \`${LARGE_SERIAL}\`
- install debug: $([[ "$INSTALL_DEBUG" -eq 1 ]] && echo "enabled" || echo "skipped")

## 요약

| 구분 | serial | device | size | density | first render(ms) | first render | scroll | touch |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
EOF

collect_device_evidence "small" "$SMALL_SERIAL"
collect_device_evidence "large" "$LARGE_SERIAL"

for row in "${SUMMARY_ROWS[@]}"; do
  echo "$row" >> "$REPORT_PATH"
done

if [[ "$HAS_FAIL" -eq 1 ]]; then
  {
    echo
    echo "## 최종 판정"
    echo "- 상태: FAIL"
    echo "- 조치: 로그/설치 실패 항목 복구 후 스크립트 재실행"
  } >> "$REPORT_PATH"
  echo "[FAIL] Wear P-004 gate failed. report=$REPORT_PATH"
  exit 1
fi

{
  echo
  echo "## 최종 판정"
  echo "- 상태: PASS (또는 WARN 포함 PASS)"
  echo "- 참고: first_render가 WARN이면 성능 최적화 액션을 TODO로 이관"
} >> "$REPORT_PATH"

echo "[PASS] Wear P-004 gate completed."
echo "       report=$REPORT_PATH"
