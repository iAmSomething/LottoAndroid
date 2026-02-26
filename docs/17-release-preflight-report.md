# 릴리즈 프리플라이트 리포트

## 실행 일시
- 2026-02-25
- 2026-02-26 (최신)

## 실행 명령
```bash
./scripts/release-preflight.sh --with-build
```
```bash
./scripts/release-final-check.sh
```

## 결과 요약
- PASS: 15
- WARN: 0
- FAIL: 0

## PASS 항목
- JDK 17 확인
- ADB 디바이스 연결(에뮬레이터 1대)
- 앱 버전 확인(`versionCode=2`, `versionName=0.2.0`)
- 스토어 스크린샷 6종 존재 및 해상도 확인(모두 `1080x2400`)
- 릴리즈 서명 값/alias 확인
- 로컬 릴리즈 키 백업 생성 및 무결성 파일(SHA-256) 생성 확인
- 품질 게이트 통과
  - `:app:ktlintCheck`
  - `:app:detekt`
  - `:app:testDebugUnitTest`
  - `:app:connectedDebugAndroidTest` (9/9)
  - `:app:assembleRelease`
- CI 모드 품질 게이트 통과
  - `./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing`

## 실행 이력
1. 초기 실행: PASS 11 / WARN 1 / FAIL 0 (서명 값 미설정)
2. `./scripts/setup-local-release-signing.sh` 적용
3. 재실행: PASS 13 / WARN 0 / FAIL 0
4. 기능/디자인/접근성 추가 반영 후 재실행(2026-02-26): PASS 13 / WARN 0 / FAIL 0 (`connectedDebugAndroidTest` 8/8)
5. 프리플라이트 로컬 게이트에 `detekt` 포함 후 재실행(2026-02-26): PASS 13 / WARN 0 / FAIL 0
6. 실기기 엄격 모드 검증(2026-02-26): PASS 13 / WARN 0 / FAIL 1 (`--require-physical-device`, 실기기 0대로 의도된 실패)
7. 실기기 엄격 모드 fail-fast 보강(2026-02-26): 실기기 0대일 때 품질게이트 생략 + 즉시 FAIL 유지 확인
8. serial 지정 모드 검증(2026-02-26): `--android-serial emulator-5554` 실행 시 PASS 14 / WARN 0 / FAIL 0
9. 엄격+serial 조합 검증(2026-02-26): `--require-physical-device --android-serial emulator-5554` 실행 시 FAIL 1(의도된 실패)
10. 에뮬레이터 간헐 이슈 관찰(2026-02-26): API 36 AVD에서 `connectedDebugAndroidTest`가 간헐적으로 `Process crashed / failed to complete startup`로 0건 실행 실패
11. 프리플라이트 보강(2026-02-26): `connectedDebugAndroidTest`에 시작/ANR 계열 실패 감지 시 1회 자동 재시도 로직 추가
12. 무기기 환경 fallback 검증(2026-02-26): `./scripts/release-final-check.sh` 실행 시 CI-only fallback 자동 전환 PASS(`--with-build-ci --skip-adb --require-signing`)
13. 앱 스타트업 안정화(2026-02-26): `AppGraph` 초기화를 lazy로 전환해 프로세스 시작 시 동기 초기화 부하 완화
14. 계측 회귀 안정화(2026-02-26): `WeeklySaveFlowInstrumentedTest`의 back 동작을 Compose dispatcher 기반으로 교체
15. 프리플라이트 재시도 확장 및 재검증(2026-02-26): 설치/연결/startup 불안정 패턴 포함 3회 재시도 적용 후 `--with-build --android-serial emulator-5554`에서 PASS 14 / WARN 0 / FAIL 0
16. 계측 플래키 후속 보정 및 최종 점검(2026-02-26): `WeeklySaveFlowInstrumentedTest` 태그/클릭 경로 안정화 후 `release-final-check.sh` 재실행 PASS 15 / WARN 0 / FAIL 0 (`connectedDebugAndroidTest` 9/9)
17. 번호관리 빠른 액션(A02) 반영 후 재검증(2026-02-26): 단위/계측/릴리즈 점검 재실행 PASS 15 / WARN 0 / FAIL 0 유지
18. stats CTA 계측 샘플 루틴 검증(2026-02-26): `run-analytics-sample-check.sh --serial emulator-5554` 실행으로 계측 3/3 + `verify-analytics-events --profile stats-cta` PASS (`docs/assets/distribution/analytics_sample_check_2026-02-26.md`)
19. 배포 주기 점검 루틴 검증(2026-02-26): `firebase-distribution-routine-check.sh` 실행으로 CI preflight PASS + Firebase dry-run PASS (`docs/assets/distribution/firebase_routine_local_2026-02-26.md`)

## 후속 조치
1. CI 환경에도 동일하게 `LOTTO_RELEASE_*` 시크릿 설정
   - 권장 명령:
     - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --dry-run`
     - `./scripts/sync-gh-release-secrets.sh --repo <owner>/<repo> --apply`
2. GitHub Actions `release-preflight.yml`에서 `--with-build-ci --skip-adb --require-signing` 실행 유지
3. 배포 직전 아래 명령으로 최종 재검증
```bash
./scripts/release-final-check.sh --require-physical-device
```
