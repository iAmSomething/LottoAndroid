# Physical Gates Routine Report

- 실행 시각: 2026-02-26 22:10:02 +0900
- date_tag: 2026-02-26
- orchestrator_exit: 2
- routine_status: BLOCKED

## 산출물
- orchestrator_report: docs/assets/distribution/physical_gates_orchestrator_2026-02-26.md
- checkpoint_report: docs/assets/distribution/physical_gates_checkpoint_2026-02-26.md
- bk_decision_report: docs/assets/distribution/performance_release_decision_checkpoint_2026-02-26.md
- wear_p4_report: docs/assets/distribution/wear_p4_device_evidence_checkpoint_2026-02-26.md

## 운영 규칙
1. routine_status=PASS: 즉시 ./scripts/sync-physical-blockers-from-checkpoint.sh --date-tag 2026-02-26 --apply 실행
2. routine_status=BLOCKED: 실기기 연결 후 오케스트레이터 재실행
3. routine_status=FAIL: 스크립트/adb 환경 오류 우선 복구
