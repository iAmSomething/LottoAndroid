# 성능 게이트 증적 패키지 스펙 (Cycle-52)

## 1. 목적
- `S08/S09` 실행 결과를 릴리즈 의사결정에 즉시 사용할 수 있는 형태로 표준화한다.
- 성능 게이트 판단 근거를 한 번에 검토할 수 있도록 증적 패키지 구성을 고정한다.

## 2. 증적 패키지 구성(S10)
| 항목 | 설명 | 필수 |
|---|---|---|
| E1 | emulator 성능 리포트(`performance_gate_emulator_<date>.md`) | 예 |
| E2 | device 성능 리포트(`performance_gate_device_<date>.md`) | 예(실기기 가능 시) |
| E3 | 판정 요약 카드(지표별 PASS/WARN/FAIL) | 예 |
| E4 | 릴리즈 결론(진행/보류) + 근거 3줄 | 예 |
| E5 | 후속 액션(다음 루틴 TODO 3개 이내) | 예 |

## 3. 패키지 판정 규칙
1. `device FAIL` 존재 시
- 결론: `보류`
- 필수 후속: `S07-5` 로그 + 롤백/최적화 작업 등록

2. `device PASS` + `emulator FAIL` 시
- 결론: `조건부 진행`
- 필수 후속: `S06/S08` 최적화 백로그 항목 추가

3. `device PASS` + `emulator WARN/PASS` 시
- 결론: `진행`
- 필수 후속: 추세 모니터링 루틴 유지

## 4. 제출 템플릿
```md
# Performance Gate Evidence <date>
- Build: <git_sha_or_build_id>
- Device profile verdict: <PASS/FAIL/PENDING>
- Emulator profile verdict: <PASS/WARN/FAIL>
- Final decision: <진행/조건부 진행/보류>
- Reason:
  1) <핵심 근거 1>
  2) <핵심 근거 2>
  3) <핵심 근거 3>
- Next actions:
  1) <다음 루틴 액션 1>
  2) <다음 루틴 액션 2>
  3) <다음 루틴 액션 3>
```

## 5. 운영 체크포인트
- [ ] E1/E2 리포트 파일 경로가 유효한가
- [ ] 판정 요약 카드가 `S02/S04/S07-4/S07-5` 항목과 일치하는가
- [ ] 최종 결론이 판정 트리 규칙(`38`)과 일치하는가
- [ ] 후속 액션이 `10-detailed-todo-board.md`에 등록되었는가

## 6. 문서 연동
- 상위 하드닝 계획: `33-reliability-and-performance-hardening-plan.md`
- 캘리브레이션 기준: `37-performance-gate-calibration-spec.md`
- 실행 템플릿/판정 트리: `38-performance-gate-execution-template.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `AX` 트랙

## 7. Cycle-53 UI 품질 게이트 연동
- `S10` 제출 시 UI 품질 게이트(`40`, `S11`) 결과를 함께 첨부해 성능/시각/상호작용 결론을 단일 결론으로 정렬한다.

## 8. Cycle-54 통합 결론 연동
- `S10` 결론은 `41-unified-quality-verdict-package-spec.md`의 `S12`에서 `S11` 결론과 통합해 최종 릴리즈 결론으로 확정한다.

## 9. Cycle-55 드라이런/에스컬레이션 연동
- `S10` 결론의 드라이런/에스컬레이션 운영은 `42-unified-verdict-dryrun-and-escalation-spec.md`를 따른다.

## 10. Cycle-56 이력/추세 연동
- `S10` 결론 이력은 `43-unified-verdict-history-and-trend-spec.md`의 레지스트리(H1)로 누적 관리한다.

## 11. Cycle-57 위험예산/프리즈 연동
- `S10` 결론의 주간 누적 위험은 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md` 기준으로 제어한다.

## 12. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S10` 보류 결론 이후 프리즈 운영 절차는 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 13. Cycle-59 프리즈 드릴/준비도 연동
- `S10` 보류 이후 대응 숙련도는 `46-freeze-drill-readiness-score-spec.md` 점수카드로 관리한다.

## 14. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S10` 보류 후속 액션의 등록/폐쇄 증적 관리는 `47-freeze-drill-corrective-action-loop-spec.md`를 따른다.

## 15. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S10` 보류 액션의 debt 점수/차단 상태 증적은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
