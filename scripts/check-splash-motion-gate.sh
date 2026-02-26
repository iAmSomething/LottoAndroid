#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if command -v rg >/dev/null 2>&1; then
  FILE_LIST_CMD=(rg --files app/src/main/java)
  FIND_SPLASH_CMD=(rg -i '(splash.*\.kt$|/splash/.*\.kt$)')
  SEARCH_CMD=(rg -n)
else
  FILE_LIST_CMD=(find app/src/main/java -type f -name "*.kt")
fi

SPLASH_FILES=()
if command -v rg >/dev/null 2>&1; then
  while IFS= read -r line; do
    [[ -n "$line" ]] && SPLASH_FILES+=("$line")
  done < <("${FILE_LIST_CMD[@]}" | "${FIND_SPLASH_CMD[@]}" || true)
else
  while IFS= read -r line; do
    [[ "$line" =~ [Ss]plash.*\.kt$ || "$line" =~ /splash/.*\.kt$ ]] && SPLASH_FILES+=("$line")
  done < <("${FILE_LIST_CMD[@]}" || true)
fi

if [[ "${#SPLASH_FILES[@]}" -eq 0 ]]; then
  echo "[PASS] No splash source detected. motion_* gate skipped."
  exit 0
fi

HAS_SHOWN=0
HAS_SKIP=0

if command -v rg >/dev/null 2>&1; then
  "${SEARCH_CMD[@]}" "(MOTION_SPLASH_SHOWN|motion_splash_shown)" "${SPLASH_FILES[@]}" >/dev/null 2>&1 && HAS_SHOWN=1
else
  grep -En "(MOTION_SPLASH_SHOWN|motion_splash_shown)" "${SPLASH_FILES[@]}" >/dev/null 2>&1 && HAS_SHOWN=1
fi

if command -v rg >/dev/null 2>&1; then
  "${SEARCH_CMD[@]}" "(MOTION_SPLASH_SKIP|motion_splash_skip)" "${SPLASH_FILES[@]}" >/dev/null 2>&1 && HAS_SKIP=1
else
  grep -En "(MOTION_SPLASH_SKIP|motion_splash_skip)" "${SPLASH_FILES[@]}" >/dev/null 2>&1 && HAS_SKIP=1
fi

if [[ "$HAS_SHOWN" -eq 1 && "$HAS_SKIP" -eq 1 ]]; then
  echo "[PASS] Splash motion analytics events are connected."
  exit 0
fi

echo "[FAIL] Splash source detected but required motion analytics events are missing."
echo "        Required: motion_splash_shown, motion_splash_skip"
echo "        Splash files:"
for file in "${SPLASH_FILES[@]}"; do
  echo "        - $file"
done
exit 1
