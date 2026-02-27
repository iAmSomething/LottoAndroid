#!/usr/bin/env bash
set -euo pipefail

TAG="WeeklyLottoAnalytics"
SERIAL=""
LOG_FILE=""
REPORT_FILE=""
OFFICIAL_FAILURE_RATE_MAX="60"
TERMINAL_FAILURE_RATE_MAX="5"
API_P95_LATENCY_MAX="2000"
STORAGE_FAILURE_RATE_MAX="5"
STORAGE_P95_LATENCY_MAX="250"
MIN_API_SAMPLES="1"
MIN_STORAGE_SAMPLES="1"

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/evaluate-ops-observability-threshold.sh \
    [--serial <adb-serial> | --log-file <path>] \
    [--report-file <path>] \
    [--official-failure-rate-max <percent>] \
    [--terminal-failure-rate-max <percent>] \
    [--api-p95-latency-max <ms>] \
    [--storage-failure-rate-max <percent>] \
    [--storage-p95-latency-max <ms>] \
    [--min-api-samples <count>] \
    [--min-storage-samples <count>]

Notes:
  - official failure rate: source=official status=failure / source=official total
  - terminal failure rate: source=mirror status=failure / source=official total
  - without --log-file, this script reads `adb logcat -d -s WeeklyLottoAnalytics:I`
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --log-file)
      LOG_FILE="${2:-}"
      shift 2
      ;;
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --official-failure-rate-max)
      OFFICIAL_FAILURE_RATE_MAX="${2:-}"
      shift 2
      ;;
    --terminal-failure-rate-max)
      TERMINAL_FAILURE_RATE_MAX="${2:-}"
      shift 2
      ;;
    --api-p95-latency-max)
      API_P95_LATENCY_MAX="${2:-}"
      shift 2
      ;;
    --storage-failure-rate-max)
      STORAGE_FAILURE_RATE_MAX="${2:-}"
      shift 2
      ;;
    --storage-p95-latency-max)
      STORAGE_P95_LATENCY_MAX="${2:-}"
      shift 2
      ;;
    --min-api-samples)
      MIN_API_SAMPLES="${2:-}"
      shift 2
      ;;
    --min-storage-samples)
      MIN_STORAGE_SAMPLES="${2:-}"
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

is_number() {
  [[ "$1" =~ ^[0-9]+([.][0-9]+)?$ ]]
}

is_integer() {
  [[ "$1" =~ ^[0-9]+$ ]]
}

for value in \
  "$OFFICIAL_FAILURE_RATE_MAX" \
  "$TERMINAL_FAILURE_RATE_MAX" \
  "$STORAGE_FAILURE_RATE_MAX"; do
  if ! is_number "$value"; then
    echo "[FAIL] invalid percent threshold: $value"
    exit 1
  fi
done

for value in \
  "$API_P95_LATENCY_MAX" \
  "$STORAGE_P95_LATENCY_MAX" \
  "$MIN_API_SAMPLES" \
  "$MIN_STORAGE_SAMPLES"; do
  if ! is_integer "$value"; then
    echo "[FAIL] invalid integer threshold: $value"
    exit 1
  fi
done

load_lines_from_adb() {
  local -a adb_cmd=("adb")
  if [[ -n "$SERIAL" ]]; then
    adb_cmd+=("-s" "$SERIAL")
  fi
  if ! "${adb_cmd[@]}" get-state >/dev/null 2>&1; then
    echo "[FAIL] No adb device connected. Provide --log-file or connect a device."
    exit 1
  fi
  "${adb_cmd[@]}" logcat -d -s "${TAG}:I"
}

if [[ -n "$LOG_FILE" ]]; then
  if [[ ! -f "$LOG_FILE" ]]; then
    echo "[FAIL] Log file not found: $LOG_FILE"
    exit 1
  fi
  lines="$(cat "$LOG_FILE")"
  source_desc="log_file:$LOG_FILE"
else
  lines="$(load_lines_from_adb)"
  source_desc="adb:${SERIAL:-auto}"
fi

if [[ -z "${lines// }" ]]; then
  echo "[FAIL] No analytics logs found."
  exit 1
fi

count_pattern() {
  local input="$1"
  local pattern="$2"
  local count
  count="$(printf "%s\n" "$input" | rg -c "$pattern" || true)"
  echo "${count:-0}"
}

extract_latency() {
  local pattern="$1"
  printf "%s\n" "$lines" \
    | rg "$pattern" \
    | sed -nE 's/.*latency_ms=([0-9]+).*/\1/p'
}

calc_rate() {
  local numerator="$1"
  local denominator="$2"
  awk -v n="$numerator" -v d="$denominator" 'BEGIN { if (d == 0) printf "0.00"; else printf "%.2f", (n * 100.0) / d }'
}

calc_p95() {
  local values="$1"
  printf "%s\n" "$values" \
    | awk 'NF { print $1 }' \
    | sort -n \
    | awk '
        { vals[NR] = $1 }
        END {
          if (NR == 0) {
            print 0
            exit
          }
          idx = int((NR * 95 + 99) / 100)
          if (idx < 1) idx = 1
          if (idx > NR) idx = NR
          print vals[idx]
        }
      '
}

is_leq() {
  local value="$1"
  local limit="$2"
  awk -v v="$value" -v l="$limit" 'BEGIN { exit(v <= l ? 0 : 1) }'
}

official_lines="$(printf "%s\n" "$lines" | rg "ops_api_request \\| .*source=official" || true)"
mirror_lines="$(printf "%s\n" "$lines" | rg "ops_api_request \\| .*source=mirror" || true)"
storage_lines="$(printf "%s\n" "$lines" | rg "ops_storage_mutation \\|" || true)"

official_total="$(count_pattern "$official_lines" "ops_api_request")"
official_failure="$(count_pattern "$official_lines" "status=failure")"
mirror_failure="$(count_pattern "$mirror_lines" "status=failure")"

storage_success="$(count_pattern "$storage_lines" "status=success")"
storage_failure="$(count_pattern "$storage_lines" "status=failure")"
storage_skipped="$(count_pattern "$storage_lines" "status=skipped")"
storage_total=$((storage_success + storage_failure))

official_failure_rate="$(calc_rate "$official_failure" "$official_total")"
terminal_failure_rate="$(calc_rate "$mirror_failure" "$official_total")"
storage_failure_rate="$(calc_rate "$storage_failure" "$storage_total")"

api_latency_values="$(extract_latency "ops_api_request \\|")"
storage_latency_values="$(extract_latency "ops_storage_mutation \\| .*status=(success|failure)")"

api_p95_latency="$(calc_p95 "$api_latency_values")"
storage_p95_latency="$(calc_p95 "$storage_latency_values")"

api_sample_ok=1
storage_sample_ok=1
official_rate_ok=1
terminal_rate_ok=1
api_latency_ok=1
storage_rate_ok=1
storage_latency_ok=1

if [[ "$official_total" -lt "$MIN_API_SAMPLES" ]]; then
  api_sample_ok=0
fi
if [[ "$storage_total" -lt "$MIN_STORAGE_SAMPLES" ]]; then
  storage_sample_ok=0
fi
if ! is_leq "$official_failure_rate" "$OFFICIAL_FAILURE_RATE_MAX"; then
  official_rate_ok=0
fi
if ! is_leq "$terminal_failure_rate" "$TERMINAL_FAILURE_RATE_MAX"; then
  terminal_rate_ok=0
fi
if ! is_leq "$api_p95_latency" "$API_P95_LATENCY_MAX"; then
  api_latency_ok=0
fi
if ! is_leq "$storage_failure_rate" "$STORAGE_FAILURE_RATE_MAX"; then
  storage_rate_ok=0
fi
if ! is_leq "$storage_p95_latency" "$STORAGE_P95_LATENCY_MAX"; then
  storage_latency_ok=0
fi

echo "[INFO] Ops observability threshold summary"
echo "  - source: $source_desc"
echo "  - api_requests(official): $official_total"
echo "  - api_official_failure_rate: ${official_failure_rate}% (max ${OFFICIAL_FAILURE_RATE_MAX}%)"
echo "  - api_terminal_failure_rate: ${terminal_failure_rate}% (max ${TERMINAL_FAILURE_RATE_MAX}%)"
echo "  - api_p95_latency_ms: ${api_p95_latency} (max ${API_P95_LATENCY_MAX})"
echo "  - storage_mutations(success+failure): $storage_total (skipped=$storage_skipped)"
echo "  - storage_failure_rate: ${storage_failure_rate}% (max ${STORAGE_FAILURE_RATE_MAX}%)"
echo "  - storage_p95_latency_ms: ${storage_p95_latency} (max ${STORAGE_P95_LATENCY_MAX})"

verdict="PASS"
if [[ "$api_sample_ok" -eq 0 || "$storage_sample_ok" -eq 0 || "$official_rate_ok" -eq 0 || "$terminal_rate_ok" -eq 0 || "$api_latency_ok" -eq 0 || "$storage_rate_ok" -eq 0 || "$storage_latency_ok" -eq 0 ]]; then
  verdict="FAIL"
fi

if [[ -n "$REPORT_FILE" ]]; then
  mkdir -p "$(dirname "$REPORT_FILE")"
  timestamp_utc="$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  {
    echo "# Ops Observability Threshold Report"
    echo
    echo "- Generated at (UTC): ${timestamp_utc}"
    echo "- Source: ${source_desc}"
    echo "- Verdict: **${verdict}**"
    echo
    echo "## Metrics"
    echo
    echo "| Metric | Value | Threshold | Result |"
    echo "|---|---:|---:|---|"
    echo "| API samples (official) | ${official_total} | >= ${MIN_API_SAMPLES} | $([[ "$api_sample_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| API official failure rate (%) | ${official_failure_rate} | <= ${OFFICIAL_FAILURE_RATE_MAX} | $([[ "$official_rate_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| API terminal failure rate (%) | ${terminal_failure_rate} | <= ${TERMINAL_FAILURE_RATE_MAX} | $([[ "$terminal_rate_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| API p95 latency (ms) | ${api_p95_latency} | <= ${API_P95_LATENCY_MAX} | $([[ "$api_latency_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| Storage samples (success+failure) | ${storage_total} | >= ${MIN_STORAGE_SAMPLES} | $([[ "$storage_sample_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| Storage failure rate (%) | ${storage_failure_rate} | <= ${STORAGE_FAILURE_RATE_MAX} | $([[ "$storage_rate_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| Storage p95 latency (ms) | ${storage_p95_latency} | <= ${STORAGE_P95_LATENCY_MAX} | $([[ "$storage_latency_ok" -eq 1 ]] && echo PASS || echo FAIL) |"
    echo "| Storage skipped count | ${storage_skipped} | n/a | INFO |"
  } > "$REPORT_FILE"
  echo "[INFO] Saved threshold report: $REPORT_FILE"
fi

if [[ "$verdict" == "PASS" ]]; then
  echo "[PASS] Ops observability thresholds satisfied."
  exit 0
fi

echo "[FAIL] Ops observability threshold check failed."
if [[ "$api_sample_ok" -eq 0 ]]; then
  echo "  - insufficient API samples: required >= ${MIN_API_SAMPLES}, got ${official_total}"
fi
if [[ "$storage_sample_ok" -eq 0 ]]; then
  echo "  - insufficient storage samples: required >= ${MIN_STORAGE_SAMPLES}, got ${storage_total}"
fi
if [[ "$official_rate_ok" -eq 0 ]]; then
  echo "  - API official failure rate exceeded: ${official_failure_rate}% > ${OFFICIAL_FAILURE_RATE_MAX}%"
fi
if [[ "$terminal_rate_ok" -eq 0 ]]; then
  echo "  - API terminal failure rate exceeded: ${terminal_failure_rate}% > ${TERMINAL_FAILURE_RATE_MAX}%"
fi
if [[ "$api_latency_ok" -eq 0 ]]; then
  echo "  - API p95 latency exceeded: ${api_p95_latency}ms > ${API_P95_LATENCY_MAX}ms"
fi
if [[ "$storage_rate_ok" -eq 0 ]]; then
  echo "  - storage failure rate exceeded: ${storage_failure_rate}% > ${STORAGE_FAILURE_RATE_MAX}%"
fi
if [[ "$storage_latency_ok" -eq 0 ]]; then
  echo "  - storage p95 latency exceeded: ${storage_p95_latency}ms > ${STORAGE_P95_LATENCY_MAX}ms"
fi
exit 1
