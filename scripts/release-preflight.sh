#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

RUN_BUILD_LOCAL=0
RUN_BUILD_CI=0
SKIP_ADB=0
REQUIRE_SIGNING=0
REQUIRE_PHYSICAL_DEVICE=0

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
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 [--with-build|--with-build-ci] [--skip-adb] [--require-signing] [--require-physical-device]"
      exit 2
      ;;
  esac
  shift
done

if [[ "$RUN_BUILD_LOCAL" -eq 1 && "$RUN_BUILD_CI" -eq 1 ]]; then
  echo "Only one of --with-build or --with-build-ci can be set."
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
  if [[ "$DEVICE_COUNT" -ge 1 ]]; then
    pass "ADB 연결 디바이스 ${DEVICE_COUNT}대 확인(실기기 ${PHYSICAL_DEVICE_COUNT}, 에뮬레이터 ${EMULATOR_DEVICE_COUNT})"
    if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 && "$PHYSICAL_DEVICE_COUNT" -lt 1 ]]; then
      fail "실기기 연결 필수 옵션 활성화됨(--require-physical-device), 현재 실기기 0대"
    fi
  else
    if [[ "$REQUIRE_PHYSICAL_DEVICE" -eq 1 ]]; then
      fail "실기기 연결 필수 옵션 활성화됨(--require-physical-device), 연결 디바이스 없음"
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

section "품질 게이트"
if [[ "$RUN_BUILD_LOCAL" -eq 1 ]]; then
  if ./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:assembleRelease; then
    pass "품질 게이트 통과(ktlint/detekt/unit/connected/release)"
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
