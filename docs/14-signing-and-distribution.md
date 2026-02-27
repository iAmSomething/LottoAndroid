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

### 3-1-0. 최종 점검 래퍼(실행 우선 권장)
```bash
./scripts/release-final-check.sh
```
- 실기기 1대가 연결되어 있으면 자동으로 실기기 엄격 모드를 사용
- 실기기가 없으면 에뮬레이터 1대를 자동 선택해 검증 진행
- ADB 디바이스가 없으면 CI-only fallback(`--with-build-ci --skip-adb --require-signing`)으로 자동 전환
- 검증 이력은 `18-device-validation-report.md`에 자동 기록

### 3-1-a. 최종 배포 직전(실기기 필수)
```bash
./scripts/release-final-check.sh --require-physical-device
```
- 실기기 1대 이상이 연결되지 않으면 실패(`FAIL > 0`)하도록 강제
- 에뮬레이터만 연결된 상태의 오검증을 방지
- 다중 실기기 환경에서 특정 단말 지정:
```bash
./scripts/release-final-check.sh --require-physical-device --serial <adb-serial>
```

### 3-1-b. 테스트 대상 serial 지정(선택)
```bash
./scripts/release-preflight.sh --with-build --android-serial <adb-serial>
```
- 다중 디바이스 연결 시 특정 단말만 대상으로 `connectedDebugAndroidTest` 실행
- `--require-physical-device`와 함께 쓰면 “지정 serial이 실기기인지”까지 검증

### 3-1-c. stats CTA 계측 샘플 점검(권장)
```bash
./scripts/run-analytics-sample-check.sh --serial <adb-serial>
```
- 실행 범위: 계측 테스트 3종 실행 후 analytics 로그 샘플 검증
- 검증 프로파일: `verify-analytics-events.sh --profile stats-cta`
- 증적 로그 저장 옵션:
```bash
./scripts/run-analytics-sample-check.sh --serial <adb-serial> --save-log docs/assets/distribution/analytics_sample_YYYY-MM-DD.log
```

### 3-1-d. 운영 관측성 샘플 점검(권장)
```bash
./scripts/run-ops-observability-check.sh --serial <adb-serial>
```
- 실행 범위: `MainNavigationInstrumentedTest`, `WeeklySaveFlowInstrumentedTest` 실행 후 ops 이벤트 검증
- 검증 프로파일: `verify-analytics-events.sh --profile ops-core`
- 증적 로그 저장 옵션:
```bash
./scripts/run-ops-observability-check.sh --serial <adb-serial> --save-log docs/assets/distribution/ops_observability_YYYY-MM-DD.log
```

### 3-1-e. 릴리즈 위험 점수 산출(권장)
```bash
./scripts/calculate-release-risk-score.sh \
  --base-ref origin/main \
  --head-ref HEAD \
  --defect-count 0 \
  --report-file docs/assets/distribution/release_risk_score_local_YYYY-MM-DD.md
```
- 산출값: 변경량/테스트 반영/결함 건수 기반 `0~100` 점수 + 위험 등급(`LOW/MEDIUM/HIGH/CRITICAL`)
- CI 자동 산출: `.github/workflows/release-risk-score.yml` (PR, 주간 스케줄, 수동 실행)

### 3-1-f. 시크릿 파일 정책 가드(권장)
```bash
./scripts/check-secret-file-policy.sh \
  --report-file docs/assets/distribution/secret_policy_guard_local_YYYY-MM-DD.md
```
- 기본 모드: 추적 파일/`.gitignore` 정책 위반을 실패로 처리하고, 로컬 파일 존재는 경고로 기록
- 엄격 모드:
```bash
./scripts/check-secret-file-policy.sh --strict-local
```
- CI 자동 점검: `.github/workflows/secret-policy-guard.yml` (PR, 주간 스케줄, 수동 실행)

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

## 6. Firebase App Distribution (TestFlight 대체)

### 6-1. 사전 준비
- Firebase App ID 확보 (예: `1:1083851357764:android:2da8bc877b0e7c89b94611`)
- 서비스 계정 키(JSON) 발급 후 로컬 보관
- 테스터 그룹 alias 준비(배포 옵션 `--groups`는 **그룹 이름이 아니라 alias** 사용)

### 6-2. 로컬 배포 스크립트
```bash
./scripts/firebase-distribute.sh \
  --project-id lottoeveryday \
  --app-id 1:1083851357764:android:2da8bc877b0e7c89b94611 \
  --service-account ./lottoeveryday-firebase-adminsdk-fbsvc-06db76153d.json \
  --group-display-name "수연이" \
  --group-alias suyeoni \
  --groups suyeoni \
  --build-task :app:assembleDebug
```

### 6-3. 운영 주의사항
- 서비스 계정 JSON은 git에 커밋하지 않는다.
- `google-services.json`, `*firebase-adminsdk*.json`은 `.gitignore`로 제외한다.
- 배포 노트는 `--release-notes` 또는 `--release-notes-file`로 명시 가능하다.

### 6-4. GitHub Actions CI/CD 체인
- 워크플로우:
  - 시크릿 파일 정책 가드: `.github/workflows/secret-policy-guard.yml`
  - 릴리즈 위험 점수 리포트: `.github/workflows/release-risk-score.yml`
  - PR/머지 품질게이트: `.github/workflows/release-preflight.yml`
  - Firebase 자동 배포: `.github/workflows/firebase-distribution.yml`
  - Firebase 주기 점검(dry-run): `.github/workflows/firebase-distribution-routine.yml`
- 트리거 체인:
  1. `push` → PR 생성
  2. PR에서 `Release Preflight` 통과
  3. PR merge 후 `main` push 발생
  4. `Release Preflight` 성공 시 `Firebase Distribution` 자동 실행
  5. Firebase App Distribution 그룹으로 release APK 배포

### 6-6. 주기 점검 루틴(dry-run)
- 로컬 점검:
```bash
./scripts/firebase-distribution-routine-check.sh \
  --project-id lottoeveryday \
  --app-id 1:1083851357764:android:2da8bc877b0e7c89b94611 \
  --groups suyeoni \
  --service-account ./lottoeveryday-firebase-adminsdk-fbsvc-06db76153d.json \
  --report-file docs/assets/distribution/firebase_routine_local_2026-02-26.md
```
- CI 점검:
  - 매주 스케줄(UTC 월요일 01:00) + 수동 실행으로 동작
  - 실행 체인: `release-preflight --with-build-ci --skip-adb --require-signing` -> `firebase-distribute --dry-run`
  - 증적: workflow artifact(`firebase-distribution-routine-<run_id>`)
  - 첫 실행 증적: run `22436650122` 성공, `docs/assets/distribution/firebase_routine_ci_22436650122.md`

### 6-5. GitHub Secrets (필수)
- 릴리즈 서명:
  - `LOTTO_RELEASE_STORE_FILE_BASE64`
  - `LOTTO_RELEASE_STORE_PASSWORD`
  - `LOTTO_RELEASE_KEY_ALIAS`
  - `LOTTO_RELEASE_KEY_PASSWORD`
- Firebase 배포:
  - `FIREBASE_PROJECT_ID` (예: `lottoeveryday`)
  - `FIREBASE_APP_ID` (예: `1:1083851357764:android:2da8bc877b0e7c89b94611`)
  - `FIREBASE_TESTER_GROUP_ALIAS` (예: `suyeoni`)
  - `FIREBASE_TESTER_GROUP_DISPLAY_NAME` (선택, 예: `수연이`)
  - `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64` (서비스계정 JSON base64)
