#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DATE_TAG="$(date +%F)"
CHECKPOINT_REPORT=""
TODO_FILE="docs/10-detailed-todo-board.md"
APPLY=0

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/sync-physical-blockers-from-checkpoint.sh [options]

Options:
  --date-tag <yyyy-mm-dd>         report date tag.
  --checkpoint-report <path>      checkpoint report path.
  --todo-file <path>              todo board file path.
  --apply                         apply changes (default: dry-run).
  -h, --help                      show this help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --date-tag)
      DATE_TAG="${2:-}"
      shift 2
      ;;
    --checkpoint-report)
      CHECKPOINT_REPORT="${2:-}"
      shift 2
      ;;
    --todo-file)
      TODO_FILE="${2:-}"
      shift 2
      ;;
    --apply)
      APPLY=1
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

if [[ -z "$CHECKPOINT_REPORT" ]]; then
  CHECKPOINT_REPORT="docs/assets/distribution/physical_gates_checkpoint_${DATE_TAG}.md"
fi

if [[ ! -f "$CHECKPOINT_REPORT" ]]; then
  echo "[FAIL] checkpoint report not found: $CHECKPOINT_REPORT"
  exit 1
fi
if [[ ! -f "$TODO_FILE" ]]; then
  echo "[FAIL] todo file not found: $TODO_FILE"
  exit 1
fi

BK_STATUS_LINE="$(rg -n "^- BK gate \\(BK-001/BK-002\\):" "$CHECKPOINT_REPORT" || true)"
P4_STATUS_LINE="$(rg -n "^- Wear P-004 gate:" "$CHECKPOINT_REPORT" || true)"

if [[ -z "$BK_STATUS_LINE" || -z "$P4_STATUS_LINE" ]]; then
  echo "[FAIL] Could not parse gate status lines from checkpoint report."
  exit 1
fi

BK_PASS=0
P4_PASS=0
if [[ "$BK_STATUS_LINE" == *"PASS"* ]]; then
  BK_PASS=1
fi
if [[ "$P4_STATUS_LINE" == *"PASS"* ]]; then
  P4_PASS=1
fi

echo "[INFO] checkpoint=$CHECKPOINT_REPORT"
echo "[INFO] BK gate pass=$BK_PASS"
echo "[INFO] Wear P-004 gate pass=$P4_PASS"

if [[ "$BK_PASS" -ne 1 && "$P4_PASS" -ne 1 ]]; then
  echo "[INFO] No blockers can be closed from current checkpoint."
  exit 0
fi

TMP_FILE="$(mktemp)"
cp "$TODO_FILE" "$TMP_FILE"

if [[ "$BK_PASS" -eq 1 ]]; then
  sed -E \
    -e 's/^- \[!\] BK-001 /- [x] BK-001 /' \
    -e 's/^- \[!\] BK-002 /- [x] BK-002 /' \
    "$TMP_FILE" > "${TMP_FILE}.next"
  mv "${TMP_FILE}.next" "$TMP_FILE"
fi

if [[ "$P4_PASS" -eq 1 ]]; then
  sed -E 's/^- \[!\] P-004 /- [x] P-004 /' "$TMP_FILE" > "${TMP_FILE}.next"
  mv "${TMP_FILE}.next" "$TMP_FILE"
fi

if cmp -s "$TODO_FILE" "$TMP_FILE"; then
  rm -f "$TMP_FILE"
  echo "[INFO] No line changes required."
  exit 0
fi

if [[ "$APPLY" -eq 1 ]]; then
  cp "$TMP_FILE" "$TODO_FILE"
  echo "[PASS] Updated todo file: $TODO_FILE"
else
  echo "[INFO] Dry-run diff:"
  diff -u "$TODO_FILE" "$TMP_FILE" || true
fi

rm -f "$TMP_FILE"
