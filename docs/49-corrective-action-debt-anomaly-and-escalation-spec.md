# 보정 액션 부채 이상징후/자동 에스컬레이션 스펙 (Cycle-64)

## 1. 목적
- `S19` 부채 지표를 실시간 운영에 연결해 debt 급증/차단 장기화 상황을 조기에 탐지한다.
- 탐지 즉시 단계별 경보와 응답 SLA를 적용해 릴리즈 제어 지연을 줄인다.

## 2. 이상징후 탐지 규칙(S20-Anomaly)
- 탐지 조건:
  - `debt_total` 3사이클 이동평균 대비 +30% 이상 급증
  - `blocked_minutes` 120분 초과
  - `overdue_count`가 1사이클 내 2건 이상 증가
  - `P0 overdue` 발생
- 탐지 결과:
  - `anomaly_level`: `L1` | `L2` | `L3`
  - `anomaly_reason`: `debt_spike` | `blocked_long` | `overdue_burst` | `p0_overdue`

## 3. 경보 단계/채널
- `L1` (주의): 채널 `release-ops` 알림 + owner 멘션
- `L2` (경계): `release-ops` + `product-lead` 동시 알림, 30분 내 대응 필요
- `L3` (심각): `release-ops` + `product-lead` + `qa-lead`, 즉시 `blocked` 재평가 회의(15분) 호출

## 4. 응답 SLA
- `ack`(경보 확인):
  - `L1`: 30분 이내
  - `L2`: 15분 이내
  - `L3`: 10분 이내
- `owner 지정`:
  - `L1`: 60분 이내
  - `L2`: 30분 이내
  - `L3`: 15분 이내
- `초기 대응 계획 등록`:
  - `L1`: 4시간 이내
  - `L2`: 2시간 이내
  - `L3`: 1시간 이내

## 5. 상태 전환 규칙
- `L2/L3`에서 SLA 위반 시 자동으로 한 단계 상향
- `L3`가 2사이클 연속 발생하면 릴리즈 상태를 기본 `blocked`로 유지
- 이상징후 해소 조건:
  - `debt_total` 2사이클 연속 하락
  - `P0 overdue = 0`
  - `blocked_minutes` 기준 이하 회복

## 6. 사후 동기화
- 경보 이벤트는 다음 문서에 동기화:
  - `S14` 추세 카드: `alert_count`, `ack_latency`, `escalation_lead_time`
  - `S19` debt 리포트: 경보 원인별 debt 기여도
  - 루틴 TODO: 미해결 경보 기반 우선순위 3개 자동 등록

## 7. 제출 템플릿
```text
[S20 Anomaly/Escalation]
- Build: <sha/tag>
- debt_total: <value>
- anomaly_level: <L1|L2|L3>
- anomaly_reason: <debt_spike|blocked_long|overdue_burst|p0_overdue>
- Alert timestamps:
  - raised_at: <YYYY-MM-DD HH:mm>
  - ack_at: <YYYY-MM-DD HH:mm>
  - owner_assigned_at: <YYYY-MM-DD HH:mm>
- SLA status:
  - ack: <PASS/FAIL>
  - owner assignment: <PASS/FAIL>
  - initial response plan: <PASS/FAIL>
- Sync status:
  - S14 trend update: <done/pending>
  - S19 debt report update: <done/pending>
  - next routine TODO: <done/pending>
```

## 8. 운영 체크포인트
- [ ] 이상징후 탐지가 규칙대로 수행됐는가
- [ ] 경보 단계/채널이 조건별로 정확히 발행됐는가
- [ ] 응답 SLA가 기준 내 유지됐는가
- [ ] 사후 동기화(`S14`/`S19`/루틴 TODO)가 누락 없이 반영됐는가

## 9. 문서 연동
- 부채/차단 정책: `48-corrective-action-debt-and-release-block-spec.md`
- 보정 액션 폐쇄 루프: `47-freeze-drill-corrective-action-loop-spec.md`
- 드릴 준비도 점수: `46-freeze-drill-readiness-score-spec.md`
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md` (`S20-1~S20-4`)
- 실행 보드: `10-detailed-todo-board.md` `BI` 트랙

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
