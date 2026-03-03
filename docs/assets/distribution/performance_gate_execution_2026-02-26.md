# 성능 게이트 실행/판정 증적 (2026-02-26)

## 1) 실행 커맨드 매트릭스 검증(S09-1)
- emulator 프로파일
  - `./scripts/run-performance-sample-check.sh --serial emulator-5554 --profile emulator --repeat 5 --warmup 1 --save-report docs/assets/distribution/performance_gate_emulator_2026-02-26.md`
- device 프로파일(검증용 실행)
  - `./scripts/run-performance-sample-check.sh --serial emulator-5554 --profile device --repeat 5 --warmup 1 --save-report docs/assets/distribution/performance_gate_device_2026-02-26.md`

## 2) 리포트 필수 필드 검증(S09-2)
- emulator/device 리포트 모두 다음 필드 포함 확인
  - `Profile`, `Device Class`, `Device`, `OS/API`, `Runs(total/warmup/measured)`
  - `Startup median/P95`, `Jank median/P95`, `ANR count(delta)`
  - `Final Verdict`, `Gate Decision`, `Release Blocking`

## 3) 판정 트리 검증(S09-3)
- 실제 판정(실기기 미보유)
  - `./scripts/evaluate-performance-gate.sh --emulator-report ... --device-report ... --save-report docs/assets/distribution/performance_release_decision_2026-02-26.md`
  - 결과: `Decision=PENDING_DEVICE_VALIDATION` (device report가 emulator class라 릴리즈 차단 근거에서 제외)
- HOLD 시나리오 시뮬레이션
  - `performance_gate_device_simulated_physical_fail_2026-02-26.md` 생성(Device Class를 `physical`로 변경)
  - `./scripts/evaluate-performance-gate.sh ... --device-report docs/assets/distribution/performance_gate_device_simulated_physical_fail_2026-02-26.md --save-report docs/assets/distribution/performance_release_decision_simulated_hold_2026-02-26.md`
  - 결과: `Decision=HOLD`, exit code 1

## 4) 릴리즈 체크 연동 검증(S04/S07-4/S07-5)
- `./scripts/release-preflight.sh --with-build --android-serial emulator-5554`
- 결과 요약: `PASS=16 WARN=1 FAIL=0`
- 성능 게이트 섹션에서 자동 수행
  - `performance_gate_emulator_2026-02-26.md` 자동 생성
  - `performance_release_decision_2026-02-26.md` 자동 생성
  - 실기기 미보유 상태를 `pending` 경고로 승격(차단 아님)

## 5) 후속 연결(S09-4)
- `P-004`(실기기 증적 확보 블로커)와 연동 유지
- 다음 실행 시 실기기 serial로 `--profile device` 리포트 생성 후 판정 상태를 `PENDING -> PROCEED/HOLD`로 전환
