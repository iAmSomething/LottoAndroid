#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v rg >/dev/null 2>&1; then
  echo "[FAIL] rg command is required for splash motion gate check."
  exit 1
fi

SPLASH_FILES=()
while IFS= read -r line; do
  [[ -n "$line" ]] && SPLASH_FILES+=("$line")
done < <(rg --files app/src/main/java | rg -i '(splash.*\.kt$|/splash/.*\.kt$)' || true)

if [[ "${#SPLASH_FILES[@]}" -eq 0 ]]; then
  echo "[PASS] No splash source detected. motion_* gate skipped."
  exit 0
fi

HAS_SHOWN=0
HAS_SKIP=0

if rg -n "(MOTION_SPLASH_SHOWN|motion_splash_shown)" "${SPLASH_FILES[@]}" >/dev/null 2>&1; then
  HAS_SHOWN=1
fi

if rg -n "(MOTION_SPLASH_SKIP|motion_splash_skip)" "${SPLASH_FILES[@]}" >/dev/null 2>&1; then
  HAS_SKIP=1
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
