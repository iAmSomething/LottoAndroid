#!/usr/bin/env bash
set -euo pipefail

PACKAGE_NAME="${1:-com.weeklylotto.app.debug}"
LOG_FILE="files/widget_refresh_history.log"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb를 찾을 수 없습니다. Android SDK platform-tools를 확인하세요." >&2
  exit 1
fi

if ! adb get-state >/dev/null 2>&1; then
  echo "연결된 adb 디바이스가 없습니다." >&2
  exit 1
fi

echo "[widget-refresh-history] package=${PACKAGE_NAME}"
if ! adb shell "pm path ${PACKAGE_NAME}" >/dev/null 2>&1; then
  echo "패키지가 설치되어 있지 않습니다: ${PACKAGE_NAME}" >&2
  exit 2
fi

if ! adb shell "run-as ${PACKAGE_NAME} test -f ${LOG_FILE}"; then
  echo "히스토리 파일이 없습니다. 위젯 갱신 이벤트를 먼저 발생시켜 주세요." >&2
  exit 3
fi

adb shell "run-as ${PACKAGE_NAME} cat ${LOG_FILE}"
