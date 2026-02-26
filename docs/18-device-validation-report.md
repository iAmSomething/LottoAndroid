# 디바이스 계측 검증 리포트

## 목적
- 배포 전 디바이스 기반 검증(실기기 우선, 필요 시 에뮬레이터 fallback) 이력을 기록한다.
- 실기기 검증 미수행 상태를 명시적으로 추적해 릴리즈 리스크를 관리한다.

## 실행 방법
```bash
./scripts/release-final-check.sh
```

옵션:
- 실기기 강제: `./scripts/release-final-check.sh --require-physical-device`
- 특정 기기 지정: `./scripts/release-final-check.sh --serial <adb-serial>`
- 문서 업데이트 생략: `./scripts/release-final-check.sh --skip-doc`

참고:
- 실기기 전용 계측 재검증만 실행할 때는 기존 스크립트 사용 가능  
  `./scripts/run-physical-device-validation.sh`
- ADB 디바이스가 전혀 없으면 `release-final-check.sh`가 CI-only fallback으로 자동 전환된다.

## 실행 이력
- 2026-02-26: 실기기 미연결로 실행 불가 (`[FAIL] No physical device connected. Connect one device and retry.`)
- 2026-02-26: 프리플라이트 엄격 모드에서도 동일 확인 (`./scripts/release-preflight.sh --with-build --require-physical-device` → `FAIL=1`)
- 2026-02-26: 엄격+serial 조합에서 에뮬레이터 serial 지정 시에도 실패 확인 (`--require-physical-device --android-serial emulator-5554` → `FAIL=1`)
- 2026-02-26: 무기기 상태에서 `release-final-check.sh` 실행 시 CI-only fallback 자동 전환 PASS 확인

## 2026-02-26 10:43:48 +0900
- 결과: FAIL(1)
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`

## 2026-02-26 11:06:16 +0900
- 결과: PASS
- 검증 모드: CI_ONLY_NO_DEVICE
- 디바이스: N/A
- Android: N/A
- ADB Serial: `N/A`
- 실행 명령: `./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing`
