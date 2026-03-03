#!/usr/bin/env bash
set -euo pipefail

ADB_BIN="${ADB_BIN:-adb}"
ADB_SERIAL="${ADB_SERIAL:-}"
OUT_DIR="${1:-/Volumes/무제/lotto/docs/assets/visual-proof-matrix}"
PKG_ACTIVITY_PRIMARY="${PKG_ACTIVITY_PRIMARY:-com.weeklylotto.app.debug/com.weeklylotto.app.MainActivity}"
PKG_ACTIVITY_FALLBACK="${PKG_ACTIVITY_FALLBACK:-com.weeklylotto.app/com.weeklylotto.app.MainActivity}"

mkdir -p "$OUT_DIR"

adb_cmd() {
  if [[ -n "$ADB_SERIAL" ]]; then
    "$ADB_BIN" -s "$ADB_SERIAL" "$@"
  else
    "$ADB_BIN" "$@"
  fi
}

launch_home() {
  if ! adb_cmd shell am start -n "$PKG_ACTIVITY_PRIMARY" >/dev/null 2>&1; then
    adb_cmd shell am start -n "$PKG_ACTIVITY_FALLBACK" >/dev/null
  fi
  sleep 1
  adb_cmd shell input tap 180 2250
  sleep 1
}

capture_png() {
  local name="$1"
  adb_cmd exec-out screencap -p > "$OUT_DIR/$name"
}

capture_set() {
  local brightness="$1"
  local font_tag="$2"
  local variant="$3"

  launch_home
  capture_png "home_${brightness}_${font_tag}_${variant}.png"

  adb_cmd shell input tap 674 827
  sleep 1
  capture_png "generator_${brightness}_${font_tag}_${variant}.png"

  adb_cmd shell input keyevent KEYCODE_BACK
  sleep 1
  adb_cmd shell input tap 540 2250
  sleep 1
  capture_png "manage_${brightness}_${font_tag}_${variant}.png"

  adb_cmd shell input tap 900 2250
  sleep 1
  capture_png "result_${brightness}_${font_tag}_${variant}.png"
}

adb_cmd start-server >/dev/null
adb_cmd wait-for-device

for brightness in normal lowlight; do
  if [[ "$brightness" == "normal" ]]; then
    adb_cmd shell cmd uimode night no >/dev/null
  else
    adb_cmd shell cmd uimode night yes >/dev/null
  fi

  adb_cmd shell settings put system font_scale 1.0
  sleep 1
  capture_set "$brightness" "1_0" "b"

  adb_cmd shell settings put system font_scale 1.3
  sleep 1
  capture_set "$brightness" "1_3" "b"
done

# Restore defaults for daily development.
adb_cmd shell settings put system font_scale 1.0
adb_cmd shell cmd uimode night no >/dev/null

echo "Captured visual proof matrix to: $OUT_DIR"
