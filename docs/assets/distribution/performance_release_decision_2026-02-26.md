# 성능 릴리즈 판정 리포트

- 실행 시각: 2026-02-26 21:02:53 +0900
- Emulator report: docs/assets/distribution/performance_gate_emulator_2026-02-26.md
- Emulator device class: emulator
- Emulator verdict: PASS
- Device report: docs/assets/distribution/performance_gate_device_2026-02-26.md
- Device class: emulator
- Device verdict: MISSING
- Decision: PENDING_DEVICE_VALIDATION
- Release Status: PENDING
- Follow-up: 실기기(device) 프로파일 성능 리포트 생성 필요

## Rule
- device FAIL -> HOLD
- device PASS + emulator WARN/FAIL -> PROCEED_WITH_OPTIMIZATION_BACKLOG
- device PASS + emulator PASS -> PROCEED
- device missing -> PENDING_DEVICE_VALIDATION
