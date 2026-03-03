# 성능 릴리즈 판정 리포트

- 실행 시각: 2026-02-26 20:46:43 +0900
- Emulator report: docs/assets/distribution/performance_gate_emulator_2026-02-26.md
- Emulator device class: emulator
- Emulator verdict: PASS
- Device report: docs/assets/distribution/performance_gate_device_simulated_physical_fail_2026-02-26.md
- Device class: physical
- Device verdict: FAIL
- Decision: HOLD
- Release Status: BLOCKED
- Follow-up: S07-5 규칙 적용: device FAIL 원인 분석 후 최적화/롤백 결정 필요

## Rule
- device FAIL -> HOLD
- device PASS + emulator WARN/FAIL -> PROCEED_WITH_OPTIMIZATION_BACKLOG
- device PASS + emulator PASS -> PROCEED
- device missing -> PENDING_DEVICE_VALIDATION
