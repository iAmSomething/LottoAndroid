#!/usr/bin/env bash
set -euo pipefail

SERIAL=""
REPORT_FILE=""
SAVE_LOG_FILE=""
PERFORMANCE_EVIDENCE_FILE=""
SKIP_CAPTURE=0
VISUAL_DIR="docs/assets/visual-proof-matrix"

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/run-ui-quality-gate.sh [options]

Options:
  --serial <adb-serial>             adb target serial.
  --report-file <path>              markdown report output path.
  --save-log <path>                 analytics log output path.
  --performance-evidence <path>     performance gate evidence markdown path.
  --skip-capture                    skip screenshot capture step.
  -h, --help                        show help.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial)
      SERIAL="${2:-}"
      shift 2
      ;;
    --report-file)
      REPORT_FILE="${2:-}"
      shift 2
      ;;
    --save-log)
      SAVE_LOG_FILE="${2:-}"
      shift 2
      ;;
    --performance-evidence)
      PERFORMANCE_EVIDENCE_FILE="${2:-}"
      shift 2
      ;;
    --skip-capture)
      SKIP_CAPTURE=1
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
  echo "[FAIL] adb not found."
  exit 1
fi

if [[ -z "$SERIAL" ]]; then
  SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
fi
if [[ -z "$SERIAL" ]]; then
  echo "[FAIL] No adb device connected. Use --serial."
  exit 1
fi
if ! adb -s "$SERIAL" get-state >/dev/null 2>&1; then
  echo "[FAIL] adb serial '$SERIAL' is not ready."
  exit 1
fi

DATE_TAG="$(date +%F)"
if [[ -z "$REPORT_FILE" ]]; then
  REPORT_FILE="docs/assets/distribution/ui_quality_gate_evidence_${DATE_TAG}.md"
fi
if [[ -z "$SAVE_LOG_FILE" ]]; then
  SAVE_LOG_FILE="docs/assets/distribution/ui_quality_gate_${DATE_TAG}.log"
fi

if [[ -z "$PERFORMANCE_EVIDENCE_FILE" ]]; then
  default_perf="docs/assets/distribution/performance_gate_evidence_${DATE_TAG}.md"
  if [[ -f "$default_perf" ]]; then
    PERFORMANCE_EVIDENCE_FILE="$default_perf"
  else
    PERFORMANCE_EVIDENCE_FILE="$(ls -1t docs/assets/distribution/performance_gate_evidence_*.md 2>/dev/null | head -n 1 || true)"
  fi
fi

linked_perf_verdict="미확정"
if [[ -n "$PERFORMANCE_EVIDENCE_FILE" && -f "$PERFORMANCE_EVIDENCE_FILE" ]]; then
  parsed_verdict="$(rg -n "^- Final decision:" "$PERFORMANCE_EVIDENCE_FILE" | head -n 1 | sed -E 's/.*Final decision:[[:space:]]*//')"
  if [[ -n "$parsed_verdict" ]]; then
    linked_perf_verdict="$parsed_verdict"
  fi
fi

mkdir -p "$(dirname "$REPORT_FILE")"
mkdir -p "$(dirname "$SAVE_LOG_FILE")"
mkdir -p "$VISUAL_DIR"

echo "[INFO] Ensure debug app is installed"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$PWD/.gradle-user-home}" \
  ./gradlew :app:installDebug

if [[ "$SKIP_CAPTURE" -eq 0 ]]; then
  echo "[INFO] Capture visual matrix"
  ADB_SERIAL="$SERIAL" ./scripts/capture-visual-matrix.sh "$VISUAL_DIR"
else
  echo "[INFO] Skip visual capture (--skip-capture)"
fi

echo "[INFO] Run reduce-motion unit test"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$PWD/.gradle-user-home}" \
  ./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest.모션축소_토글시_store에_즉시_저장된다"

TEST_CLASSES="com.weeklylotto.app.WeeklySaveFlowInstrumentedTest,com.weeklylotto.app.StatsCtaInstrumentedTest,com.weeklylotto.app.ManageFilterSheetInstrumentedTest,com.weeklylotto.app.SettingsPurchaseRedirectInstrumentedTest,com.weeklylotto.app.ExternalOpenFallbackDialogInstrumentedTest"

echo "[INFO] Clear analytics logcat buffer ($SERIAL)"
adb -s "$SERIAL" logcat -c

echo "[INFO] Run UI gate instrumentation tests"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$PWD/.gradle-user-home}" \
ANDROID_SERIAL="$SERIAL" \
  ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class="$TEST_CLASSES"

TMP_LOG_FILE="$(mktemp)"
adb -s "$SERIAL" logcat -d -s WeeklyLottoAnalytics:I > "$TMP_LOG_FILE"
cp "$TMP_LOG_FILE" "$SAVE_LOG_FILE"

cta_count="$(rg -c "interaction_cta_press" "$TMP_LOG_FILE" || true)"
sheet_apply_count="$(rg -c "interaction_sheet_apply" "$TMP_LOG_FILE" || true)"

missing_u1=0
for f in \
  "home_normal_1_0_b.png" "home_normal_1_3_b.png" \
  "manage_normal_1_0_b.png" "manage_normal_1_3_b.png" \
  "result_normal_1_0_b.png" "result_normal_1_3_b.png"; do
  if [[ ! -f "$VISUAL_DIR/$f" ]]; then
    missing_u1=$((missing_u1 + 1))
  fi
done

missing_u2=0
for screen in home generator manage result; do
  for brightness in normal lowlight; do
    for scale in 1_0 1_3; do
      if [[ ! -f "$VISUAL_DIR/${screen}_${brightness}_${scale}_b.png" ]]; then
        missing_u2=$((missing_u2 + 1))
      fi
    done
  done
done

missing_u4=0
for screen in home generator manage result; do
  if [[ ! -f "$VISUAL_DIR/${screen}_lowlight_1_3_b.png" ]]; then
    missing_u4=$((missing_u4 + 1))
  fi
done

u1_status="PASS"
u2_status="PASS"
u3_status="PASS"
u4_status="PASS"
if [[ "$missing_u1" -gt 0 ]]; then
  u1_status="FAIL"
fi
if [[ "$missing_u2" -gt 0 ]]; then
  u2_status="FAIL"
fi
if [[ "$cta_count" -eq 0 || "$sheet_apply_count" -eq 0 ]]; then
  u3_status="FAIL"
fi
if [[ "$missing_u4" -gt 0 ]]; then
  u4_status="FAIL"
fi

ui_quality_verdict="진행"
if [[ "$u1_status" != "PASS" || "$u2_status" != "PASS" || "$u3_status" != "PASS" || "$u4_status" != "PASS" ]]; then
  ui_quality_verdict="조건부 진행"
fi

final_decision="$ui_quality_verdict"
if [[ "$linked_perf_verdict" == "보류" ]]; then
  final_decision="보류"
elif [[ "$linked_perf_verdict" == "조건부 진행" && "$ui_quality_verdict" == "진행" ]]; then
  final_decision="조건부 진행"
fi

build_ref="$(git rev-parse --short HEAD)"

cat > "$REPORT_FILE" <<EOF
# UI Quality Gate Evidence (${DATE_TAG})

- Build: ${build_ref}
- U1 Typography: ${u1_status}
- U2 Visual Consistency: ${u2_status}
- U3 Interaction Resilience: ${u3_status}
- U4 Accessibility & Low-end: ${u4_status}
- Linked Performance Verdict(S10): ${linked_perf_verdict}
- UI verdict(S11): ${ui_quality_verdict}
- Final decision: ${final_decision}

## 실행 명령
\`\`\`bash
ADB_SERIAL=${SERIAL} ./scripts/capture-visual-matrix.sh ${VISUAL_DIR}
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest.모션축소_토글시_store에_즉시_저장된다"
ANDROID_SERIAL=${SERIAL} ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=${TEST_CLASSES}
\`\`\`

## 근거
1. U1/U2: \`${VISUAL_DIR}\` 매트릭스 파일 점검(누락 U1=${missing_u1}, U2=${missing_u2}).
2. U3: \`WeeklyLottoAnalytics\` 로그에서 \`interaction_cta_press=${cta_count}\`, \`interaction_sheet_apply=${sheet_apply_count}\` 확인 + fallback 계측 테스트 통과.
3. U4: 저조도+1.3x 캡처 누락 \`${missing_u4}\`, Reduce Motion 저장 회귀 테스트 통과.

## 증적 파일
- Visual matrix: \`${VISUAL_DIR}\`
- Analytics log: \`${SAVE_LOG_FILE}\`
- Linked S10 evidence: \`${PERFORMANCE_EVIDENCE_FILE:-N/A}\`

## Next actions
1. BK-001 실기기 device 성능 리포트 생성.
2. BK-002 성능 판정 재평가 후 통합 결론 재확정.
3. BK-003 S06 핫패스 성능 증적 리포트 작성.
EOF

rm -f "$TMP_LOG_FILE"
echo "[PASS] UI quality gate report generated: $REPORT_FILE"
