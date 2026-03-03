# Unified Verdict Weekly Review (2026-W09)

- Total builds: 2
- Final decisions: 진행 0 / 조건부 0 / 보류 2
- Decision risk ratio(조건부+보류): 100%
- Avg lead time(min): 11.5
- Action completion rate: 33.3% (BK-003 완료, BK-001~BK-002 미완료)
- Repeated escalation codes: E13-1:2, E13-2:0, E13-3:0, E13-4:0
- Weekly verdict: FAIL

## H1. 이력 레지스트리
| build_id | s10_verdict | s11_verdict | applied_rule | escalation_code | final_decision | lead_time_min |
|---|---|---|---|---|---|---|
| 7fb46eb | 보류 | 보류 | R1 | E13-1 | 보류 | 11 |
| 7fb46eb-sim-hold | 보류 | 진행 | R1 | E13-1 | 보류 | 12 |

## H2. 주간 추세 카드
- 조건부/보류 비율: 100% (기준 40% 초과, FAIL)
- 평균 리드타임: 11.5분 (기준 PASS)
- 액션 이행률: 0% (기준 80% 미만, FAIL)
- alert_count: 1 (L2)
- ack_latency: 11분
- escalation_lead_time: 22분

## H3. 반복 이슈 클러스터
- Cluster-C1 (`E13-1`, 실기기 미검증 기반 보류): 2건
- 승격 판정: 관찰 유지(2주 내 3회 이상 시 P1 승격)

## H4. 액션 이행률
- Total actions: 3
- Done actions: 1 (`BK-003`)
- Open actions: 2 (`BK-001`, `BK-002`)

## H5. 다음 루틴 우선순위
1. BK-001 실기기 device 성능 리포트 생성으로 `PENDING/HOLD` 원인 제거.
2. BK-002 성능 판정 재평가 후 통합 결론(`S12`) 재확정.
3. BQ-002 핫패스 프로파일링 템플릿 기반 실측 증적 보강.

## 판정 코멘트
- 이번 주 결론은 성능 실기기 증적 부재로 보류 편향이 높으며, 실제 릴리즈 판단을 위해 BK 트랙 3건을 최우선으로 처리해야 한다.
