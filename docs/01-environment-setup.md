# 환경 설정 가이드

## 1. 필수 환경
- macOS
- JDK 17
- Android Studio 최신 안정 버전
- Android SDK Platform 35

## 2. 프로젝트 구성
- 루트에 Android 단일 앱 모듈(`:app`)
- 패키지: `com.weeklylotto.app`
- Gradle Wrapper: 8.10.2
- Android Gradle Plugin: 8.8.2
- Kotlin: 2.0.21

## 3. 초기 실행
```bash
./gradlew tasks
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## 4. 릴리즈 서명 준비
- 로컬 파일: `keystore.properties` (예시: `keystore.properties.sample`)
- 또는 환경변수:
  - `LOTTO_RELEASE_STORE_FILE`
  - `LOTTO_RELEASE_STORE_PASSWORD`
  - `LOTTO_RELEASE_KEY_ALIAS`
  - `LOTTO_RELEASE_KEY_PASSWORD`

## 5. 주요 의존성
- Compose BOM
- Room
- WorkManager
- Glance
- CameraX + ML Kit Barcode

## 6. 품질 도구
- `ktlint` (코드 스타일)
- `detekt` (정적 분석)
- GitHub Actions CI (`.github/workflows/android-ci.yml`)
- GitHub Actions 릴리즈 프리플라이트 (`.github/workflows/release-preflight.yml`)

## 7. CI 시크릿(릴리즈 서명)
- `LOTTO_RELEASE_STORE_FILE_BASE64` (`weeklylotto-release.jks` base64 문자열)
- `LOTTO_RELEASE_STORE_PASSWORD`
- `LOTTO_RELEASE_KEY_ALIAS`
- `LOTTO_RELEASE_KEY_PASSWORD`
- 동기화 스크립트:
  - 점검: `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --dry-run`
  - 적용: `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --apply`

## 8. 로컬 체크리스트
- [ ] `JAVA_HOME`가 JDK 17을 가리키는지 확인
- [ ] Android SDK 설치 및 `local.properties` 확인
- [ ] 첫 빌드 후 에뮬레이터에서 홈 화면 진입 확인
- [ ] 계측 테스트 전 `adb devices`로 연결 기기 확인
