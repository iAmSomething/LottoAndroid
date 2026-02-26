# 릴리즈 체크리스트

## 1. 빌드/품질
- [x] `./gradlew :app:assembleRelease`
- [x] `./gradlew :app:testDebugUnitTest`
- [x] `./gradlew :app:ktlintCheck :app:detekt`
- [x] `./gradlew :app:connectedDebugAndroidTest` (실기기/에뮬레이터 필수)
- [x] 실기기 재검증 자동화 명령 준비(`./scripts/run-physical-device-validation.sh`)
- [x] 프리플라이트 실기기 엄격 모드 추가(`./scripts/release-preflight.sh --with-build --require-physical-device`)
- [x] 배포 전 최종 점검 래퍼 추가(`./scripts/release-final-check.sh`)
- [x] 크래시 재현 케이스 점검 (계측 테스트 재실행 + 수동 instrumentation 확인)
- [x] `./scripts/release-preflight.sh --with-build` 실행 및 리포트 저장(`17-release-preflight-report.md`)
- [x] CI 프리플라이트 워크플로우 추가(`.github/workflows/release-preflight.yml`)
- [x] stats CTA 로그 샘플 검증 루틴 추가(`./scripts/run-analytics-sample-check.sh`, `verify-analytics-events --profile stats-cta`)
- [x] Firebase 배포 주기 점검 워크플로우 추가(`.github/workflows/firebase-distribution-routine.yml`, dry-run 체인)
- [x] Firebase 배포 주기 점검 첫 CI 실행 증적 확보(run `22436650122`, artifact `firebase_routine_ci_22436650122.md`)

## 2. 기능 확인
- [x] 번호 생성/잠금/저장 (단위 + 계측 스모크)
- [x] QR 등록(정상/오류) (파서 단위 + QR 수동입력 계측)
- [x] 당첨 조회/채점/하이라이트 (채점 단위 + 결과 화면 계측 진입)
- [x] 알림 예약/취소/재예약 (SettingsViewModel 단위 저장/재설정)
- [x] 위젯 A/B 표시 (WidgetDataProvider 단위 스냅샷)
- [x] 통계 지표 표시 (StatsViewModel 단위 계산/기간필터)

## 3. 스토어 배포 준비
- [x] 앱 설명 문구 초안 작성 (`12-store-metadata.md`)
- [x] 앱 아이콘/스크린샷 최종본 준비 (`docs/assets/store-screenshots/*.png`)
- [x] adaptive launcher icon 최종본 반영(`ic_launcher_foreground/background/monochrome`)
- [x] 버전코드/버전명 업데이트 (`versionCode=2`, `versionName=0.2.0`)
- [x] 개인정보/권한 고지 초안 작성 (`13-privacy-and-permissions.md`)
- [x] 서명/배포 절차 문서화 (`14-signing-and-distribution.md`)
- [x] 서명 키/릴리즈 키 보관 확인 (로컬 키 + 백업 생성, `17-release-preflight-report.md`)

## 4. 운영 인수인계
- [x] 문서 00~11 최신화
- [x] 릴리즈 문서 12~14 추가
- [x] 장애 대응 루틴 공유 (`15-incident-response-runbook.md`)
- [x] 다음 스프린트 백로그 정리 (`16-next-sprint-backlog.md`)

## 5. 2026-02-25 사전 점검 결과
- API 36 AVD에서 `connectedDebugAndroidTest` 9/9 통과(재실행 포함 검증)
- `ktlintCheck`, `detekt`, `testDebugUnitTest`, `assembleDebug`, `assembleRelease`는 통과
- 배포 직전에는 실제 기기 1대 이상에서 계측 테스트를 1회 추가 검증 권장

## 6. 자동 검증 근거(추가)
- `connectedDebugAndroidTest`: 9/9 통과
- 신규 계측: `MainNavigationInstrumentedTest`, `QrManualFlowInstrumentedTest`
- 신규 단위: `SettingsViewModelTest`, `DefaultWidgetDataProviderTest`, `StatsViewModelTest`, `BallChipAccessibilityTest`, `TicketDetailShareFormatterTest`, `ColorContrastTest`
- 당첨 API fallback: 공식 API 실패 시 미러 API로 1212회 로드 확인
- CI preflight 모드(`--with-build-ci --skip-adb --require-signing`) 검증 통과

## 7. 최종 배포 직전 실행 명령
1. 공통(자동 대상 선택: 실기기 우선, 없으면 에뮬레이터)
```bash
./scripts/release-final-check.sh
```
- 통과 조건: 요약 `FAIL=0`
- 실기기 없을 때는 에뮬레이터 검증으로 진행되며, 실기기 검증은 `pending`으로 남김
- ADB 디바이스가 전혀 없으면 CI-only fallback(`--with-build-ci --skip-adb --require-signing`)으로 자동 전환

2. 스토어 제출 직전(실기기 필수 강제)
```bash
./scripts/release-final-check.sh --require-physical-device
```
- 통과 조건: 실기기 1대 이상 연결 + 요약 `FAIL=0`
- 다중 실기기 연결 시:
```bash
./scripts/release-final-check.sh --require-physical-device --serial <adb-serial>
```
