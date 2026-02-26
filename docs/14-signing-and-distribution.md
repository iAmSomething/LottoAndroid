# 서명키/배포 프로세스

## 1. 릴리즈 서명 구성
- 파일: `keystore.properties` (로컬 전용, git ignore)
- 샘플: `keystore.properties.sample`
- 로컬 자동화: `scripts/setup-local-release-signing.sh`
- 환경변수 대체 지원:
  - `LOTTO_RELEASE_STORE_FILE`
  - `LOTTO_RELEASE_STORE_PASSWORD`
  - `LOTTO_RELEASE_KEY_ALIAS`
  - `LOTTO_RELEASE_KEY_PASSWORD`
- CI 시크릿(워크플로우용):
  - `LOTTO_RELEASE_STORE_FILE_BASE64`
  - `LOTTO_RELEASE_STORE_PASSWORD`
  - `LOTTO_RELEASE_KEY_ALIAS`
  - `LOTTO_RELEASE_KEY_PASSWORD`

## 2. 우선순위 규칙
1. `keystore.properties` 값 사용
2. 미존재 시 환경변수 사용
3. 둘 다 없으면 `release` 빌드는 debug 서명으로 fallback

## 3. 배포 전 빌드 명령
```bash
./gradlew :app:clean
./gradlew :app:assembleRelease
```

## 3-1. 프리플라이트 자동 점검(권장)
```bash
./scripts/release-preflight.sh --with-build
```
- 점검 범위: JDK/ADB, 버전, 스크린샷 세트, 서명 값, 품질 게이트
- 최신 실행 기록: `17-release-preflight-report.md`

### 3-1-a. 최종 배포 직전(실기기 필수)
```bash
./scripts/release-preflight.sh --with-build --require-physical-device
```
- 실기기 1대 이상이 연결되지 않으면 실패(`FAIL > 0`)하도록 강제
- 에뮬레이터만 연결된 상태의 오검증을 방지

## 3-1-1. CI 프리플라이트(ADB 없이)
```bash
./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing
```
- 점검 범위: `ktlint`, `detekt`, `unit test`, `assembleRelease`
- 워크플로우: `.github/workflows/release-preflight.yml`
- 이벤트별 동작:
  - `pull_request`: 서명 비필수 모드(`--with-build-ci --skip-adb`)
  - `push/workflow_dispatch`: 서명 필수 모드(`--with-build-ci --skip-adb --require-signing`)

## 3-2. 로컬 릴리즈 키 자동 생성(초기 1회)
```bash
./scripts/setup-local-release-signing.sh
```
- 생성 결과:
  - `keystore.properties` 자동 생성/정규화
  - `~/.weeklylotto/keys/weeklylotto-release.jks` 생성
  - `~/.weeklylotto/keys/backup/`에 키 백업 + SHA-256 기록

## 3-3. GitHub Secrets 동기화(권장)
```bash
./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --dry-run
./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --apply
```
- 민감값을 콘솔 출력하지 않고 GitHub Secrets로 직접 업로드
- 기본 모드는 `dry-run`이며, 실제 업로드는 `--apply`를 명시해야 수행
- 안전장치:
  - 대상 repo에 `app/build.gradle.kts`가 없으면 업로드 차단
  - Android 프로젝트가 아닌 repo에 강제로 업로드하려면 `--force` 필요
  - `--repo` 미지정 시 사용자 계정에서 Android repo를 자동 탐지(단일 후보일 때만)

## 4. 권장 운영 절차
1. 릴리즈 키는 팀 공유 드라이브/비밀관리 도구에 이중 보관
2. `keystore.properties`는 각 개발자 로컬에서만 관리
3. CI 배포는 시크릿(`LOTTO_RELEASE_*`) 기반으로만 구성
4. 배포 직전 `07-release-checklist.md` 전 항목 점검

## 5. 산출물 경로
- APK: `app/build/outputs/apk/release/`
