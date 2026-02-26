#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

RUN_BUILD_LOCAL=0
RUN_BUILD_CI=0
SKIP_ADB=0
REQUIRE_SIGNING=0
REQUIRE_PHYSICAL_DEVICE=0
TARGET_ANDROID_SERIAL="${ANDROID_SERIAL:-}"
SKIP_BUILD_DUE_TO_ENV_FAIL=0

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
        echo "Usage: $0 [--with-build|--with-build-ci] [--skip-adb] [--require-signing] [--require-physical-device] [--android-serial <serial>]"
        exit 2
      fi
      TARGET_ANDROID_SERIAL="$2"
      shift
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--with-build|--with-build-ci] [--skip-adb] [--require-signing] [--require-physical-device] [--android-serial <serial>]"
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

run_connected_test_with_retry() {
  local serial="$1"
  local attempt=1

  while [[ "$attempt" -le 3 ]]; do
    local log_file
    local status
    log_file="$(mktemp)"

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

section "요약"
echo "PASS=${PASS_COUNT} WARN=${WARN_COUNT} FAIL=${FAIL_COUNT}"

if [[ "$FAIL_COUNT" -gt 0 ]]; then
  exit 1
fi

exit 0
