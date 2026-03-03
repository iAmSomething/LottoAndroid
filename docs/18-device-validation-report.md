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
- `release-final-check.sh`는 Wear `P-004` probe를 함께 실행해 `wear_p4_device_evidence_release_probe_<date>.md`를 갱신한다.
- `run-all-physical-gates-when-ready.sh`는 폰+Wear 실기기 조건 충족 시 `BK/P-004/checkpoint`를 순차 실행한다.
- 실기기 연결 즉시 실행 표준 커맨드/완료 판정은 `72-physical-gates-immediate-execution-bundle.md`를 기준으로 사용한다.
- `10`/`11`/`18` 블로커 상태 동기화는 `73-physical-blocker-state-sync-checklist.md`를 기준으로 점검한다.
- `run-physical-gates-routine-check.sh`는 오케스트레이터 결과를 `PASS/BLOCKED/FAIL` 루틴 리포트로 저장한다.
- `blocked` 장기화 리스크 판정은 `71-blocked-state-longtail-risk-criteria.md` 기준을 따른다.

## 실행 이력
- 2026-02-26: 실기기 미연결로 실행 불가 (`[FAIL] No physical device connected. Connect one device and retry.`)
- 2026-02-26: 프리플라이트 엄격 모드에서도 동일 확인 (`./scripts/release-preflight.sh --with-build --require-physical-device` → `FAIL=1`)
- 2026-02-26: 엄격+serial 조합에서 에뮬레이터 serial 지정 시에도 실패 확인 (`--require-physical-device --android-serial emulator-5554` → `FAIL=1`)
- 2026-02-26: 무기기 상태에서 `release-final-check.sh` 실행 시 CI-only fallback 자동 전환 PASS 확인
- 2026-02-26: Wear `P-004` release probe blocked 리포트 생성(`wear_p4_device_evidence_release_probe_2026-02-26.md`)
- 2026-02-26: 물리 게이트 오케스트레이터 timeout 리허설 blocked(`physical_gates_orchestrator_2026-02-26.md`)
- 2026-02-26: 물리 게이트 루틴 리포트 생성(`physical_gates_routine_2026-02-26.md`, `BLOCKED`)
- 2026-03-03: Wear `P-004` blocked 증적 최신화(`wear_p4_device_evidence_2026-03-03.md`, 실기기 2대 미연결)
- 2026-03-03: 물리 게이트 오케스트레이터 blocked 최신화(`physical_gates_orchestrator_2026-03-03.md`, `physical_gates_checkpoint_2026-03-03.md`)
- 2026-03-03: BK/P-004 체크포인트 blocked 증적 생성(`performance_release_decision_checkpoint_2026-03-03.md`, `wear_p4_device_evidence_checkpoint_2026-03-03.md`)

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

## 2026-02-26 11:14:24 +0900
- 결과: PASS
- 검증 모드: CI_ONLY_NO_DEVICE
- 디바이스: N/A
- Android: N/A
- ADB Serial: `N/A`
- 실행 명령: `./scripts/release-preflight.sh --with-build-ci --skip-adb --require-signing`

## 2026-02-26 17:04:13 +0900
- 결과: PASS
- 검증 모드: EMULATOR_CI_FALLBACK
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`
- 보완 메모: 에뮬레이터 계측 품질게이트 실패로 CI-only 게이트로 대체 통과

## 2026-02-26 17:46:28 +0900
- 결과: PASS
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`

## 2026-02-26 17:56:41 +0900
- 결과: PASS
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`

## 2026-02-26 18:11:38 +0900
- 결과: PASS
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`

## 2026-02-26 18:15:39 +0900
- 결과: PASS
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`

## 2026-02-26 18:20:10 +0900
- 결과: PASS
- 검증 모드: EMULATOR
- 디바이스: Google sdk_gphone64_arm64
- Android: 16 (SDK 36)
- ADB Serial: `emulator-5554`
- 실행 명령: `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`
