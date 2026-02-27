#!/usr/bin/env bash
set -euo pipefail

REPEAT=3
TASK=":app:testDebugUnitTest"
REPORT_FILE=""
TEST_PATTERN=""

usage() {
  cat <<'EOF'
Usage: scripts/run-unit-flaky-guard.sh [options]

Options:
  --repeat N           Number of repeated runs (default: 3)
  --task TASK          Gradle task to run (default: :app:testDebugUnitTest)
  --tests PATTERN      Optional Gradle --tests pattern
  --report-file PATH   Output markdown report path
  -h, --help           Show help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repeat)
      REPEAT="${2:-}"
      shift 2
      ;;
    --task)
      TASK="${2:-}"
      shift 2
      ;;
    --tests)
      TEST_PATTERN="${2:-}"
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

if ! [[ "$REPEAT" =~ ^[0-9]+$ ]] || [[ "$REPEAT" -lt 1 ]]; then
  echo "--repeat must be a positive integer" >&2
  exit 1
fi

timestamp_utc="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
date_stamp="$(date '+%Y-%m-%d')"
if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/unit_flaky_guard_${date_stamp}.md"
fi

mkdir -p "$(dirname "$REPORT_FILE")"

pass_count=0
fail_count=0
total_duration=0
report_dir="$(dirname "$REPORT_FILE")"
logs_dir="${report_dir}/unit_flaky_logs_${date_stamp}_$(date '+%H%M%S')"
mkdir -p "$logs_dir"

declare -a run_statuses=()
declare -a run_durations=()
declare -a run_log_paths=()

for run in $(seq 1 "$REPEAT"); do
  log_path="${logs_dir}/run_${run}.log"
  run_log_paths+=("$log_path")

  cmd=(./gradlew "$TASK" --rerun-tasks)
  if [[ -n "$TEST_PATTERN" ]]; then
    cmd+=(--tests "$TEST_PATTERN")
  fi

  start_sec="$(date +%s)"
  if "${cmd[@]}" >"$log_path" 2>&1; then
    status="PASS"
    ((pass_count+=1))
  else
    status="FAIL"
    ((fail_count+=1))
  fi
  end_sec="$(date +%s)"
  duration_sec=$((end_sec - start_sec))
  total_duration=$((total_duration + duration_sec))

  run_statuses+=("$status")
  run_durations+=("$duration_sec")
done

{
  echo "# Unit Test Flaky Guard Report"
  echo
  echo "- Generated at (UTC): ${timestamp_utc}"
  echo "- Task: \`${TASK}\`"
  if [[ -n "$TEST_PATTERN" ]]; then
    echo "- Tests filter: \`${TEST_PATTERN}\`"
  fi
  echo "- Repeats: ${REPEAT}"
  echo "- Pass: ${pass_count}"
  echo "- Fail: ${fail_count}"
  echo "- Total duration: ${total_duration}s"
  echo
  echo "| Run | Status | Duration(s) |"
  echo "|---:|---|---:|"
  for idx in "${!run_statuses[@]}"; do
    run_no=$((idx + 1))
    echo "| ${run_no} | ${run_statuses[$idx]} | ${run_durations[$idx]} |"
  done
  echo
  echo "## Run Logs"
  for idx in "${!run_log_paths[@]}"; do
    run_no=$((idx + 1))
    echo "- Run ${run_no}: \`${run_log_paths[$idx]}\`"
  done
} >"$REPORT_FILE"

echo "[flaky-guard] report: ${REPORT_FILE}"
echo "[flaky-guard] repeats=${REPEAT} pass=${pass_count} fail=${fail_count} total=${total_duration}s"

if [[ "$fail_count" -gt 0 ]]; then
  exit 1
fi
