#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

# Keep Gradle caches inside the workspace to avoid home-partition disk pressure.
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$ROOT_DIR/.gradle-user-home}"

RUN_BUILD_LOCAL=0
RUN_BUILD_CI=0
SKIP_ADB=0
REQUIRE_SIGNING=0
REQUIRE_PHYSICAL_DEVICE=0
TARGET_ANDROID_SERIAL="${ANDROID_SERIAL:-}"
SKIP_BUILD_DUE_TO_ENV_FAIL=0
SKIP_PERFORMANCE_GATE=0
PERFORMANCE_PROFILE="auto"
PERFORMANCE_REPEAT=5
PERFORMANCE_WARMUP=1
PERFORMANCE_BASELINE_REPORT=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --with-build)
      RUN_BUILD_LOCAL=1
      ;;
    --with-build-ci)
      RUN_BUILD_CI=1
      ;;
    --skip-adb)
      SKIP_ADB=1
      ;;
    --require-signing)
      REQUIRE_SIGNING=1
      ;;
    --require-physical-device)
      REQUIRE_PHYSICAL_DEVICE=1
      ;;
    --android-serial)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --android-serial"
        echo "Usage: $0 [--with-build|--with-build-ci] [--skip-adb] [--require-signing] [--require-physical-device] [--android-serial <serial>] [--skip-performance-gate] [--performance-profile <auto|emulator|device>] [--performance-repeat <N>] [--performance-warmup <N>] [--performance-baseline-report <path>]"
        exit 2
      fi
      TARGET_ANDROID_SERIAL="$2"
      shift
      ;;
    --skip-performance-gate)
      SKIP_PERFORMANCE_GATE=1
      ;;
    --performance-profile)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --performance-profile"
        exit 2
      fi
      PERFORMANCE_PROFILE="$2"
      shift
      ;;
    --performance-repeat)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --performance-repeat"
        exit 2
      fi
      PERFORMANCE_REPEAT="$2"
      shift
      ;;
    --performance-warmup)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --performance-warmup"
        exit 2
      fi
      PERFORMANCE_WARMUP="$2"
      shift
      ;;
    --performance-baseline-report)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --performance-baseline-report"
        exit 2
      fi
      PERFORMANCE_BASELINE_REPORT="$2"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--with-build|--with-build-ci] [--skip-adb] [--require-signing] [--require-physical-device] [--android-serial <serial>] [--skip-performance-gate] [--performance-profile <auto|emulator|device>] [--performance-repeat <N>] [--performance-warmup <N>] [--performance-baseline-report <path>]"
      exit 2
      ;;
  esac
  shift
done

if [[ "$RUN_BUILD_LOCAL" -eq 1 && "$RUN_BUILD_CI" -eq 1 ]]; then
  echo "Only one of --with-build or --with-build-ci can be set."
  exit 2
fi

if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 && "$SKIP_ADB" -eq 1 ]]; then
  echo "--require-physical-device cannot be used with --skip-adb."
  exit 2
fi

if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 && "$RUN_BUILD_CI" -eq 1 ]]; then
  echo "--require-physical-device is only supported with --with-build."
  exit 2
fi

if [[ -n "$TARGET_ANDROID_SERIAL" && "$RUN_BUILD_CI" -eq 1 ]]; then
  echo "--android-serial is only supported with --with-build."
  exit 2
fi

if [[ -n "$TARGET_ANDROID_SERIAL" && "$SKIP_ADB" -eq 1 ]]; then
  echo "--android-serial cannot be used with --skip-adb."
  exit 2
fi

if [[ "$PERFORMANCE_PROFILE" != "auto" && "$PERFORMANCE_PROFILE" != "emulator" && "$PERFORMANCE_PROFILE" != "device" ]]; then
  echo "--performance-profile must be one of: auto, emulator, device."
  exit 2
fi

if ! [[ "$PERFORMANCE_REPEAT" =~ ^[0-9]+$ ]] || [[ "$PERFORMANCE_REPEAT" -lt 1 ]]; then
  echo "--performance-repeat must be a positive integer."
  exit 2
fi

if ! [[ "$PERFORMANCE_WARMUP" =~ ^[0-9]+$ ]]; then
  echo "--performance-warmup must be zero or a positive integer."
  exit 2
fi

PASS_COUNT=0
WARN_COUNT=0
FAIL_COUNT=0

pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  echo "[PASS] $1"
}

warn() {
  WARN_COUNT=$((WARN_COUNT + 1))
  echo "[WARN] $1"
}

fail() {
  FAIL_COUNT=$((FAIL_COUNT + 1))
  echo "[FAIL] $1"
}

section() {
  echo
  echo "== $1 =="
}

search_first_line() {
  local pattern="$1"
  local file="$2"
  if command -v rg >/dev/null 2>&1; then
    rg "$pattern" "$file" | head -n 1
  else
    grep -E "$pattern" "$file" | head -n 1
  fi
}

extract_prop_from_file() {
  local file="$1"
  local key="$2"
  if [[ ! -f "$file" ]]; then
    return 0
  fi
  awk -F '=' -v k="$key" '$1==k {print substr($0, index($0, "=")+1)}' "$file" | tail -n 1
}

is_emulator_serial() {
  [[ "$1" =~ ^emulator- ]]
}

resolve_connected_serial_for_perf() {
  if [[ -n "$TARGET_ANDROID_SERIAL" ]]; then
    echo "$TARGET_ANDROID_SERIAL"
    return
  fi
  adb devices | awk 'NR>1 && $2=="device" {print $1; exit}'
}

resolve_performance_profile() {
  local serial="$1"
  if [[ "$PERFORMANCE_PROFILE" == "auto" ]]; then
    if is_emulator_serial "$serial"; then
      echo "emulator"
    else
      echo "device"
    fi
    return
  fi
  echo "$PERFORMANCE_PROFILE"
}

latest_performance_report_for_date() {
  local profile="$1"
  local date_key="$2"
  ls -1t "docs/assets/distribution/performance_gate_${profile}_${date_key}"*.md 2>/dev/null | head -n 1
}

run_connected_test_with_retry() {
  local serial="$1"
  local attempt=1

  prepare_device_for_instrumentation() {
    local target_serial="$1"
    if [[ -z "$target_serial" ]]; then
      return 0
    fi

    # Make instrumentation deterministic by clearing persisted app state and waking the emulator.
    adb -s "$target_serial" shell pm clear com.weeklylotto.app.debug >/dev/null 2>&1 || true
    adb -s "$target_serial" shell pm clear com.weeklylotto.app.debug.test >/dev/null 2>&1 || true
    adb -s "$target_serial" shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true
    adb -s "$target_serial" shell input keyevent 82 >/dev/null 2>&1 || true
    adb -s "$target_serial" shell wm dismiss-keyguard >/dev/null 2>&1 || true
  }

  while [[ "$attempt" -le 3 ]]; do
    local log_file
    local status
    log_file="$(mktemp)"

    if [[ -n "$serial" ]]; then
      prepare_device_for_instrumentation "$serial"
    fi

    if [[ -n "$serial" ]]; then
      ANDROID_SERIAL="$serial" ./gradlew :app:connectedDebugAndroidTest 2>&1 | tee "$log_file"
      status="${PIPESTATUS[0]}"
    else
      ./gradlew :app:connectedDebugAndroidTest 2>&1 | tee "$log_file"
      status="${PIPESTATUS[0]}"
    fi

    if [[ "$status" -eq 0 ]]; then
      rm -f "$log_file"
      return 0
    fi

    if [[ "$attempt" -ge 3 ]]; then
      rm -f "$log_file"
      return "$status"
    fi

    if grep -Eq "Process crashed|Starting 0 tests|failed to complete startup|Failed to install split APK|Broken pipe|device offline|Exception thrown during onBeforeAll" "$log_file"; then
      warn "connectedDebugAndroidTest 일시 실패 감지(에뮬레이터/설치/시작 불안정). 재시도($attempt/3)"
      if command -v adb >/dev/null 2>&1; then
        adb reconnect offline >/dev/null 2>&1 || true
        adb start-server >/dev/null 2>&1 || true
        if [[ -n "$serial" ]]; then
          adb -s "$serial" wait-for-device >/dev/null 2>&1 || true
          for _ in {1..20}; do
            boot="$(adb -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
            if [[ "$boot" == "1" ]]; then
              break
            fi
            sleep 1
          done
        else
          adb wait-for-device >/dev/null 2>&1 || true
        fi
      fi
      sleep 5
      attempt=$((attempt + 1))
      rm -f "$log_file"
      continue
    fi

    rm -f "$log_file"
    return "$status"
  done

  return 1
}

run_local_quality_gate() {
  local serial="$1"

  if ! ./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest; then
    return 1
  fi

  if ! run_connected_test_with_retry "$serial"; then
    return 1
  fi

  if ! ./gradlew :app:assembleRelease; then
    return 1
  fi

  return 0
}

section "환경"
if command -v java >/dev/null 2>&1; then
  JAVA_MAJOR="$(java -version 2>&1 | awk -F '[\".]' '/version/ {print $2; exit}')"
  if [[ "$JAVA_MAJOR" == "17" ]]; then
    pass "JDK 17 확인"
  else
    fail "JDK 17 필요(현재: ${JAVA_MAJOR:-unknown})"
  fi
else
  fail "java 명령을 찾지 못함"
fi

if [[ "$SKIP_ADB" -eq 1 ]]; then
  pass "ADB 점검 생략(--skip-adb)"
elif command -v adb >/dev/null 2>&1; then
  DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" {count++} END {print count+0}')"
  PHYSICAL_DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {count++} END {print count+0}')"
  EMULATOR_DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" && $1 ~ /^emulator-/ {count++} END {print count+0}')"
  PHYSICAL_SERIAL_LIST="$(adb devices | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1}')"

  if [[ "$DEVICE_COUNT" -ge 1 ]]; then
    pass "ADB 연결 디바이스 ${DEVICE_COUNT}대 확인(실기기 ${PHYSICAL_DEVICE_COUNT}, 에뮬레이터 ${EMULATOR_DEVICE_COUNT})"

    if [[ -n "$TARGET_ANDROID_SERIAL" ]]; then
      if adb devices | awk 'NR>1 && $2=="device" {print $1}' | grep -qx "$TARGET_ANDROID_SERIAL"; then
        pass "지정된 테스트 대상 serial 확인(${TARGET_ANDROID_SERIAL})"
      else
        fail "지정된 serial이 연결 상태가 아님: ${TARGET_ANDROID_SERIAL}"
        SKIP_BUILD_DUE_TO_ENV_FAIL=1
      fi
    fi

    if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 && "$PHYSICAL_DEVICE_COUNT" -lt 1 ]]; then
      fail "실기기 연결 필수 옵션 활성화됨(--require-physical-device), 현재 실기기 0대"
      SKIP_BUILD_DUE_TO_ENV_FAIL=1
    fi

    if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 && "$PHYSICAL_DEVICE_COUNT" -ge 1 ]]; then
      if [[ -z "$TARGET_ANDROID_SERIAL" ]]; then
        if [[ "$PHYSICAL_DEVICE_COUNT" -eq 1 ]]; then
          TARGET_ANDROID_SERIAL="$PHYSICAL_SERIAL_LIST"
          pass "실기기 테스트 대상 자동 선택(${TARGET_ANDROID_SERIAL})"
        else
          fail "실기기 ${PHYSICAL_DEVICE_COUNT}대 감지됨. --android-serial <serial>로 대상을 지정하세요."
          SKIP_BUILD_DUE_TO_ENV_FAIL=1
        fi
      elif ! grep -qx "$TARGET_ANDROID_SERIAL" <<< "$PHYSICAL_SERIAL_LIST"; then
        fail "지정된 serial(${TARGET_ANDROID_SERIAL})은 실기기가 아님(에뮬레이터 또는 미연결)"
        SKIP_BUILD_DUE_TO_ENV_FAIL=1
      fi
    fi
  else
    if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 ]]; then
      fail "실기기 연결 필수 옵션 활성화됨(--require-physical-device), 연결 디바이스 없음"
      SKIP_BUILD_DUE_TO_ENV_FAIL=1
    else
      warn "ADB 연결 디바이스 없음(계측 테스트 자동 실행 불가)"
    fi
  fi
else
  warn "adb 명령을 찾지 못함"
fi

section "앱 버전"
VERSION_CODE="$(search_first_line '^[[:space:]]*versionCode[[:space:]]*=' app/build.gradle.kts | sed -E 's/.*=[[:space:]]*([0-9]+).*/\1/')"
VERSION_NAME="$(search_first_line '^[[:space:]]*versionName[[:space:]]*=' app/build.gradle.kts | sed -E 's/.*=[[:space:]]*"([^"]+)".*/\1/')"
if [[ -n "$VERSION_CODE" && -n "$VERSION_NAME" ]]; then
  pass "버전 확인(versionCode=${VERSION_CODE}, versionName=${VERSION_NAME})"
else
  fail "버전 코드/이름을 파싱하지 못함"
fi

section "스토어 스크린샷"
SHOT_DIR="docs/assets/store-screenshots"
EXPECTED=(home manage result generator qr_scan stats)
if [[ -d "$SHOT_DIR" ]]; then
  pass "스크린샷 폴더 존재"
else
  fail "스크린샷 폴더 누락: $SHOT_DIR"
fi

for name in "${EXPECTED[@]}"; do
  file="$SHOT_DIR/${name}.png"
  if [[ ! -f "$file" ]]; then
    fail "스크린샷 누락: $file"
    continue
  fi

  if command -v sips >/dev/null 2>&1; then
    width="$(sips -g pixelWidth "$file" 2>/dev/null | awk '/pixelWidth/ {print $2}')"
    height="$(sips -g pixelHeight "$file" 2>/dev/null | awk '/pixelHeight/ {print $2}')"
    if [[ -n "$width" && -n "$height" ]]; then
      if [[ "$width" -ge 320 && "$height" -ge 320 ]]; then
        pass "${name}.png (${width}x${height})"
      else
        fail "${name}.png 해상도 부족(${width}x${height})"
      fi
    else
      warn "${name}.png 해상도 확인 실패"
    fi
  else
    pass "${name}.png 존재 확인"
  fi

done

section "릴리즈 서명"
PROP_FILE="keystore.properties"
STORE_FILE=""
STORE_PASSWORD=""
KEY_ALIAS=""
KEY_PASSWORD=""

if [[ -f "$PROP_FILE" ]]; then
  STORE_FILE="$(extract_prop_from_file "$PROP_FILE" "storeFile")"
  STORE_PASSWORD="$(extract_prop_from_file "$PROP_FILE" "storePassword")"
  KEY_ALIAS="$(extract_prop_from_file "$PROP_FILE" "keyAlias")"
  KEY_PASSWORD="$(extract_prop_from_file "$PROP_FILE" "keyPassword")"
fi

STORE_FILE="${STORE_FILE:-${LOTTO_RELEASE_STORE_FILE:-}}"
STORE_PASSWORD="${STORE_PASSWORD:-${LOTTO_RELEASE_STORE_PASSWORD:-}}"
KEY_ALIAS="${KEY_ALIAS:-${LOTTO_RELEASE_KEY_ALIAS:-}}"
KEY_PASSWORD="${KEY_PASSWORD:-${LOTTO_RELEASE_KEY_PASSWORD:-}}"

if [[ -n "$STORE_FILE" && -n "$STORE_PASSWORD" && -n "$KEY_ALIAS" && -n "$KEY_PASSWORD" ]]; then
  if [[ -f "$STORE_FILE" ]]; then
    pass "릴리즈 서명 값 존재(storeFile/keyAlias 포함)"
    if command -v keytool >/dev/null 2>&1; then
      if keytool -list -keystore "$STORE_FILE" -storepass "$STORE_PASSWORD" -alias "$KEY_ALIAS" >/dev/null 2>&1; then
        pass "keytool alias 검증 성공(${KEY_ALIAS})"
      else
        if [[ "$REQUIRE_SIGNING" -eq 1 ]]; then
          fail "keytool alias 검증 실패(비밀번호/alias 또는 파일 확인 필요)"
        else
          warn "keytool alias 검증 실패(비밀번호/alias 또는 파일 확인 필요)"
        fi
      fi
    else
      if [[ "$REQUIRE_SIGNING" -eq 1 ]]; then
        fail "keytool 명령 미설치로 키 검증 불가"
      else
        warn "keytool 명령 미설치로 키 검증 생략"
      fi
    fi
  else
    fail "storeFile 경로가 존재하지 않음: $STORE_FILE"
  fi
else
  if [[ "$REQUIRE_SIGNING" -eq 1 ]]; then
    fail "릴리즈 서명 값 미완비(keystore.properties 또는 환경변수 설정 필요)"
  else
    warn "릴리즈 서명 값 미완비(keystore.properties 또는 환경변수 설정 필요)"
  fi
fi

section "모션 계측 게이트"
if ./scripts/check-splash-motion-gate.sh; then
  pass "스플래시 motion 이벤트 게이트 통과"
else
  fail "스플래시 motion 이벤트 게이트 실패"
fi

section "품질 게이트"
if [[ "$SKIP_BUILD_DUE_TO_ENV_FAIL" -eq 1 ]]; then
  warn "실기기 필수 조건 미충족으로 품질 게이트 실행 생략"
elif [[ "$RUN_BUILD_LOCAL" -eq 1 ]]; then
  if run_local_quality_gate "$TARGET_ANDROID_SERIAL"; then
    if [[ -n "$TARGET_ANDROID_SERIAL" ]]; then
      pass "품질 게이트 통과(ktlint/detekt/unit/connected/release, serial=${TARGET_ANDROID_SERIAL})"
    else
      pass "품질 게이트 통과(ktlint/detekt/unit/connected/release)"
    fi
  elif [[ -n "$TARGET_ANDROID_SERIAL" ]]; then
    fail "품질 게이트 실패(Gradle 로그 확인 필요, serial=${TARGET_ANDROID_SERIAL})"
  else
    fail "품질 게이트 실패(Gradle 로그 확인 필요)"
  fi
elif [[ "$RUN_BUILD_CI" -eq 1 ]]; then
  if ./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleRelease; then
    pass "품질 게이트 통과(CI: ktlint/detekt/unit/release)"
  else
    fail "품질 게이트 실패(CI Gradle 로그 확인 필요)"
  fi
else
  warn "품질 게이트 실행 생략(--with-build 또는 --with-build-ci 옵션으로 실행 가능)"
fi

section "성능 게이트"
if [[ "$SKIP_PERFORMANCE_GATE" -eq 1 ]]; then
  warn "성능 게이트 생략(--skip-performance-gate)"
elif [[ "$SKIP_BUILD_DUE_TO_ENV_FAIL" -eq 1 ]]; then
  warn "환경 조건 미충족으로 성능 게이트 실행 생략"
elif [[ "$RUN_BUILD_LOCAL" -ne 1 ]]; then
  warn "성능 게이트는 --with-build 로컬 실행에서만 동작"
elif [[ "$SKIP_ADB" -eq 1 ]]; then
  warn "성능 게이트 실행 생략(--skip-adb)"
elif ! command -v adb >/dev/null 2>&1; then
  warn "adb 명령 미설치로 성능 게이트 실행 불가"
else
  PERF_SERIAL="$(resolve_connected_serial_for_perf)"
  if [[ -z "$PERF_SERIAL" ]]; then
    warn "연결된 디바이스가 없어 성능 게이트 실행 불가"
  else
    PERF_PROFILE="$(resolve_performance_profile "$PERF_SERIAL")"
    PERF_DATE="$(date '+%Y-%m-%d')"
    PERF_REPORT_PATH="docs/assets/distribution/performance_gate_${PERF_PROFILE}_${PERF_DATE}.md"

    PERF_CMD=(
      ./scripts/run-performance-sample-check.sh
      --serial "$PERF_SERIAL"
      --profile "$PERF_PROFILE"
      --repeat "$PERFORMANCE_REPEAT"
      --warmup "$PERFORMANCE_WARMUP"
      --save-report "$PERF_REPORT_PATH"
    )

    if [[ -n "$PERFORMANCE_BASELINE_REPORT" ]]; then
      PERF_CMD+=(--baseline-report "$PERFORMANCE_BASELINE_REPORT")
    fi

    "${PERF_CMD[@]}"
    PERF_STATUS=$?

    if [[ "$PERF_STATUS" -ne 0 ]]; then
      if [[ "$PERF_PROFILE" == "emulator" ]]; then
        warn "성능 게이트 FAIL(emulator). 릴리즈 차단 없이 최적화 백로그로 이관"
      else
        fail "성능 게이트 FAIL(device). 릴리즈 보류 규칙(S07-5) 적용 필요"
      fi
    else
      if [[ "$PERF_PROFILE" == "device" ]]; then
        pass "성능 게이트 PASS(device): ${PERF_REPORT_PATH}"
      else
        pass "성능 게이트 실행 완료(${PERF_PROFILE}): ${PERF_REPORT_PATH}"
      fi
    fi

    DECISION_REPORT_PATH="docs/assets/distribution/performance_release_decision_${PERF_DATE}.md"
    EMULATOR_REPORT_TODAY="$(latest_performance_report_for_date emulator "$PERF_DATE")"
    DEVICE_REPORT_TODAY="$(latest_performance_report_for_date device "$PERF_DATE")"

    DECISION_CMD=(
      ./scripts/evaluate-performance-gate.sh
      --save-report "$DECISION_REPORT_PATH"
    )
    if [[ -n "$EMULATOR_REPORT_TODAY" ]]; then
      DECISION_CMD+=(--emulator-report "$EMULATOR_REPORT_TODAY")
    fi
    if [[ -n "$DEVICE_REPORT_TODAY" ]]; then
      DECISION_CMD+=(--device-report "$DEVICE_REPORT_TODAY")
    fi

    "${DECISION_CMD[@]}"
    DECISION_STATUS=$?

    if [[ "$DECISION_STATUS" -eq 0 ]]; then
      if grep -q "Decision: PENDING_DEVICE_VALIDATION" "$DECISION_REPORT_PATH"; then
        warn "성능 릴리즈 판정 pending(실기기 리포트 필요): ${DECISION_REPORT_PATH}"
      else
        pass "성능 릴리즈 판정 리포트 생성: ${DECISION_REPORT_PATH}"
      fi
    else
      fail "성능 릴리즈 판정 HOLD: ${DECISION_REPORT_PATH}"
    fi
  fi
fi

section "요약"
echo "PASS=${PASS_COUNT} WARN=${WARN_COUNT} FAIL=${FAIL_COUNT}"

if [[ "$FAIL_COUNT" -gt 0 ]]; then
  exit 1
fi

exit 0
