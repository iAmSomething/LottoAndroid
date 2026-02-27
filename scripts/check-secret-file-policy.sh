#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

REPORT_FILE=""
STRICT_LOCAL=0

usage() {
  cat <<'EOF'
Usage: scripts/check-secret-file-policy.sh [options]

Options:
  --report-file PATH   Markdown report output path
  --strict-local       Fail when local secret candidate files exist
  -h, --help           Show help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --strict-local)
      STRICT_LOCAL=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

date_stamp="$(date '+%Y-%m-%d')"
timestamp_utc="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/secret_policy_guard_${date_stamp}.md"
fi
mkdir -p "$(dirname "$REPORT_FILE")"

required_ignore_patterns=(
  "keystore.properties"
  "*.jks"
  "*.keystore"
  "google-services.json"
  "*firebase-adminsdk*.json"
)

missing_ignore_patterns=()
for pattern in "${required_ignore_patterns[@]}"; do
  if ! grep -Fxq "$pattern" .gitignore; then
    missing_ignore_patterns+=("$pattern")
  fi
done

tracked_matches="$(
  git ls-files | awk '
    /(^|\/)google-services\.json$/ {print; next}
    /(^|\/)keystore\.properties$/ {print; next}
    /firebase-adminsdk/ && /\.json$/ {print; next}
    /\.jks$/ {print; next}
    /\.keystore$/ {print; next}
  '
)"

local_candidates="$(
  find . \
    -type d \( -name .git -o -name .gradle -o -name build \) -prune -o \
    -type f \
    \( -name "google-services.json" -o -name "*firebase-adminsdk*.json" -o -name "keystore.properties" -o -name "*.jks" -o -name "*.keystore" \) \
    -print | sed 's|^\./||'
)"

missing_count="${#missing_ignore_patterns[@]}"
tracked_count="$(printf '%s\n' "$tracked_matches" | sed '/^$/d' | wc -l | tr -d ' ')"
local_count="$(printf '%s\n' "$local_candidates" | sed '/^$/d' | wc -l | tr -d ' ')"

status="PASS"
recommendation="정책 위반 없음. 기본 점검 유지."
if [[ "$missing_count" -gt 0 || "$tracked_count" -gt 0 ]]; then
  status="FAIL"
  recommendation=".gitignore/추적 파일 정책 위반이 있어 즉시 수정 필요."
elif [[ "$STRICT_LOCAL" -eq 1 && "$local_count" -gt 0 ]]; then
  status="FAIL"
  recommendation="strict-local 모드에서 로컬 시크릿 파일이 감지되어 실패."
elif [[ "$local_count" -gt 0 ]]; then
  status="WARN"
  recommendation="로컬 시크릿 파일이 존재함. 커밋 제외 상태를 점검."
fi

{
  echo "# Secret File Policy Guard Report"
  echo
  echo "- Generated at (UTC): ${timestamp_utc}"
  echo "- Strict local mode: ${STRICT_LOCAL}"
  echo
  echo "## Required .gitignore Patterns"
  echo
  echo "| Pattern | Present |"
  echo "|---|---|"
  for pattern in "${required_ignore_patterns[@]}"; do
    present="yes"
    if ! grep -Fxq "$pattern" .gitignore; then
      present="no"
    fi
    echo "| \`${pattern}\` | ${present} |"
  done
  echo
  echo "## Violations"
  echo
  echo "- Missing ignore patterns: ${missing_count}"
  if [[ "$missing_count" -gt 0 ]]; then
    for pattern in "${missing_ignore_patterns[@]}"; do
      echo "  - ${pattern}"
    done
  fi
  echo "- Tracked secret-like files: ${tracked_count}"
  if [[ "$tracked_count" -gt 0 ]]; then
    printf '%s\n' "$tracked_matches" | sed '/^$/d' | sed 's/^/  - /'
  fi
  echo "- Local secret-like files found: ${local_count}"
  if [[ "$local_count" -gt 0 ]]; then
    printf '%s\n' "$local_candidates" | sed '/^$/d' | sed 's/^/  - /'
  fi
  echo
  echo "## Result"
  echo
  echo "- Status: **${status}**"
  echo "- Recommendation: ${recommendation}"
} >"$REPORT_FILE"

echo "[secret-policy] report: ${REPORT_FILE}"
echo "[secret-policy] status=${status} missing=${missing_count} tracked=${tracked_count} local=${local_count}"

if [[ "$status" == "FAIL" ]]; then
  exit 1
fi
