# 물리 게이트 주간 요약 리포트 템플릿

## 1. 목적
- `run-all-physical-gates-when-ready.sh` 실행 결과를 주간 단위로 비교 가능한 형태로 고정한다.
- 실기기 부재 시에도 `BLOCKED` 상태 변동(유지/악화/해소)을 한 장에서 판단한다.

## 2. 입력 산출물
- `docs/assets/distribution/physical_gates_orchestrator_<date>.md`
- `docs/assets/distribution/physical_gates_checkpoint_<date>.md`
- `docs/assets/distribution/performance_release_decision_checkpoint_<date>.md`
- `docs/assets/distribution/wear_p4_device_evidence_checkpoint_<date>.md`

## 3. 출력 파일 규칙
- 경로: `docs/assets/distribution/physical_gates_weekly_summary_<yyyy-w##>.md`
- 예시: `docs/assets/distribution/physical_gates_weekly_summary_2026-w10.md`

## 4. 작성 템플릿
```md
# Physical Gates Weekly Summary

- 주차: <yyyy-w##>
- 작성 시각: <yyyy-mm-dd hh:mm:ss +0900>
- 기준 기간: <yyyy-mm-dd ~ yyyy-mm-dd>

## 주간 결론
- Overall: <PASS | BLOCKED | FAIL>
- BK gate: <PASS | BLOCKED | FAIL>
- Wear P-004 gate: <PASS | BLOCKED | FAIL>
- 운영 판단: <유지 | 주의 | 에스컬레이션>

## 실행 이력
| 날짜 | orchestrator_exit | BK 상태 | Wear 상태 | 근거 파일 |
|---|---:|---|---|---|
| <date-1> | <0/2/1> | <PASS/BLOCKED/FAIL> | <PASS/BLOCKED/FAIL> | `<physical_gates_orchestrator_...md>` |
| <date-2> | <0/2/1> | <PASS/BLOCKED/FAIL> | <PASS/BLOCKED/FAIL> | `<physical_gates_orchestrator_...md>` |

## 전주 대비 비교
| 항목 | 전주 | 이번 주 | 변화 |
|---|---|---|---|
| BK gate | <상태> | <상태> | <유지/개선/악화> |
| Wear P-004 gate | <상태> | <상태> | <유지/개선/악화> |
| BLOCKED 지속 일수 | <n> | <n> | <+/-n> |

## 핵심 근거
- BK 판단: `<performance_release_decision_checkpoint_...md>` (Decision/Release Status)
- Wear 판단: `<wear_p4_device_evidence_checkpoint_...md>` (상태/사유)
- 통합 판단: `<physical_gates_checkpoint_...md>` (BK/Wear 동시 상태)

## 다음 액션 (최대 3개)
1. <실기기 연결 조건 또는 즉시 실행 항목>
2. <문서 동기화 항목 (`10`/`11`/`18`)>
3. <에스컬레이션 필요 시 담당/기한>
```

## 5. 최소 검증 체크리스트
- [ ] 이번 주 실행 로그가 1회 이상 반영되었는가
- [ ] BK/Wear 상태가 checkpoint 리포트와 일치하는가
- [ ] 전주 대비 변화(유지/개선/악화)가 명시되었는가
- [ ] 다음 액션 3개 이내로 정리되었는가
