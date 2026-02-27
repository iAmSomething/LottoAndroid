#!/usr/bin/env bash
set -euo pipefail

BASE_REF="origin/main"
HEAD_REF="HEAD"
DEFECT_COUNT=0
REPORT_FILE=""

usage() {
  cat <<'EOF'
Usage: scripts/calculate-release-risk-score.sh [options]

Options:
  --base-ref REF        Base git ref for diff (default: origin/main)
  --head-ref REF        Head git ref for diff (default: HEAD)
  --defect-count N      Known unresolved defects count (default: 0)
  --report-file PATH    Markdown report output path
  -h, --help            Show help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base-ref)
      BASE_REF="${2:-}"
      shift 2
      ;;
    --head-ref)
      HEAD_REF="${2:-}"
      shift 2
      ;;
    --defect-count)
      DEFECT_COUNT="${2:-}"
      shift 2
      ;;
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
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

if ! [[ "$DEFECT_COUNT" =~ ^[0-9]+$ ]]; then
  echo "--defect-count must be a non-negative integer" >&2
  exit 1
fi

git rev-parse --verify "$BASE_REF" >/dev/null 2>&1 || {
  echo "Invalid --base-ref: $BASE_REF" >&2
  exit 1
}
git rev-parse --verify "$HEAD_REF" >/dev/null 2>&1 || {
  echo "Invalid --head-ref: $HEAD_REF" >&2
  exit 1
}

merge_base="$(git merge-base "$BASE_REF" "$HEAD_REF")"
diff_range="${merge_base}..${HEAD_REF}"

date_stamp="$(date '+%Y-%m-%d')"
timestamp_utc="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/release_risk_score_${date_stamp}.md"
fi
mkdir -p "$(dirname "$REPORT_FILE")"

files_changed="$(git diff --name-only "$diff_range" | sed '/^$/d' | wc -l | tr -d ' ')"
docs_changed="$(git diff --name-only "$diff_range" -- 'docs/**' | sed '/^$/d' | wc -l | tr -d ' ')"
app_main_changed="$(git diff --name-only "$diff_range" -- 'app/src/main/**' | sed '/^$/d' | wc -l | tr -d ' ')"
test_changed="$(git diff --name-only "$diff_range" -- 'app/src/test/**' 'app/src/androidTest/**' | sed '/^$/d' | wc -l | tr -d ' ')"
workflow_changed="$(git diff --name-only "$diff_range" -- '.github/workflows/**' | sed '/^$/d' | wc -l | tr -d ' ')"
script_changed="$(git diff --name-only "$diff_range" -- 'scripts/**' | sed '/^$/d' | wc -l | tr -d ' ')"

read -r lines_added lines_deleted <<EOF
$(git diff --numstat "$diff_range" | awk '
  $1 ~ /^[0-9]+$/ && $2 ~ /^[0-9]+$/ {add += $1; del += $2}
  END {printf "%d %d", add + 0, del + 0}
')
EOF
lines_total=$((lines_added + lines_deleted))

non_docs_changed=$((files_changed - docs_changed))

change_score=$((files_changed * 2 + lines_total / 120 + app_main_changed * 3 + workflow_changed * 4 + script_changed * 2))
if [[ "$non_docs_changed" -eq 0 ]]; then
  change_score=5
fi
if [[ "$change_score" -gt 60 ]]; then
  change_score=60
fi

test_score=0
if [[ "$app_main_changed" -gt 0 && "$test_changed" -eq 0 ]]; then
  test_score=20
elif [[ "$app_main_changed" -gt 0 ]]; then
  test_score=5
fi

defect_score=$((DEFECT_COUNT * 10))
if [[ "$defect_score" -gt 30 ]]; then
  defect_score=30
fi

total_score=$((change_score + test_score + defect_score))
if [[ "$total_score" -gt 100 ]]; then
  total_score=100
fi

risk_level="LOW"
if [[ "$total_score" -ge 75 ]]; then
  risk_level="CRITICAL"
elif [[ "$total_score" -ge 50 ]]; then
  risk_level="HIGH"
elif [[ "$total_score" -ge 25 ]]; then
  risk_level="MEDIUM"
fi

{
  echo "# Release Risk Score Report"
  echo
  echo "- Generated at (UTC): ${timestamp_utc}"
  echo "- Base ref: \`${BASE_REF}\`"
  echo "- Head ref: \`${HEAD_REF}\`"
  echo "- Diff range: \`${diff_range}\`"
  echo
  echo "## Inputs"
  echo
  echo "| Metric | Value |"
  echo "|---|---:|"
  echo "| Files changed | ${files_changed} |"
  echo "| Docs changed | ${docs_changed} |"
  echo "| App main changed | ${app_main_changed} |"
  echo "| Test files changed | ${test_changed} |"
  echo "| Workflow files changed | ${workflow_changed} |"
  echo "| Script files changed | ${script_changed} |"
  echo "| Lines added | ${lines_added} |"
  echo "| Lines deleted | ${lines_deleted} |"
  echo "| Known defects | ${DEFECT_COUNT} |"
  echo
  echo "## Score Breakdown"
  echo
  echo "| Component | Score | Note |"
  echo "|---|---:|---|"
  echo "| Change score | ${change_score} | 변경량/핵심 경로 변경량 기반(최대 60) |"
  echo "| Test score | ${test_score} | 앱 코드 변경 대비 테스트 반영 여부(최대 20) |"
  echo "| Defect score | ${defect_score} | 미해결 결함 수 기반(최대 30) |"
  echo "| **Total** | **${total_score}** | **0~100** |"
  echo
  echo "## Result"
  echo
  echo "- Risk level: **${risk_level}**"
  case "$risk_level" in
    LOW)
      echo "- Recommendation: 기본 품질 게이트 통과 시 진행"
      ;;
    MEDIUM)
      echo "- Recommendation: 품질 게이트 + 증적 리포트 검토 후 진행"
      ;;
    HIGH)
      echo "- Recommendation: 보강 테스트 또는 단계적 배포 조건부 진행"
      ;;
    CRITICAL)
      echo "- Recommendation: 즉시 릴리즈 보류, 위험 완화 후 재평가"
      ;;
  esac
} >"$REPORT_FILE"

echo "[release-risk] report: ${REPORT_FILE}"
echo "[release-risk] total_score=${total_score} level=${risk_level}"
