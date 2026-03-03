# 보정 액션 부채/릴리즈 차단 정책 스펙 (Cycle-61)

## 1. 목적
- `S18` 폐쇄 루프에서 관리되는 보정 액션을 "부채(debt) 지표"로 집계해 릴리즈 진행/차단 판단을 일관되게 만든다.
- 위험 누적 상태에서 조건부 진행이 반복되는 문제를 줄이고, 차단 해제 기준을 명확히 고정한다.

## 2. 부채 산출 규칙(S19-Debt)
- 액션 점수:
  - `P0 open/in_progress/blocked`: 8점
  - `P1 open/in_progress/blocked`: 5점
  - `P2 open/in_progress/blocked`: 2점
- 추가 가중치:
  - `overdue`: +3점
  - `reopen`: +2점
  - `exception_active`: +2점
- 합산:
  - `debt_total = Σ(action_score + 가중치)`
  - `debt_burndown = (전주 debt_total - 금주 debt_total) / 전주 debt_total * 100`

## 3. 릴리즈 차단/해제 기준
- 차단(`blocked`) 트리거:
  - `debt_total >= 20` 또는 `P0 overdue >= 1`
- 조건부 진행(`guarded`) 트리거:
  - `debt_total 10~19` and `P0 overdue = 0`
- 진행(`go`) 트리거:
  - `debt_total <= 9` and `P0/P1 overdue = 0`
- 해제(`unblock`) 조건:
  - 직전 차단 사유 해소 + 핵심 액션 2건 이상 `closed`
  - `debt_total <= 12`로 회복 + 재평가 회의 승인 완료

## 4. 예외 승인(Temporary Exception) 규칙
- 필수 필드:
  - `reason`, `owner`, `expires_at`, `mitigation`, `reviewer`
- 제한:
  - 기본 TTL 72시간
  - 동일 사유 연속 2회 초과 금지(3회차는 자동 `blocked`)
- 만료 처리:
  - `expires_at` 경과 시 자동 재평가
  - 해소 실패 시 즉시 `blocked` 재전환

## 5. 운영 리듬
- 일일:
  - `debt_total`, `overdue_count`, `blocked_minutes` 업데이트
- 루틴(사이클):
  - `S14` 추세 카드에 `debt_burndown`, `blocked_minutes`, `exception_active_count` 기록
- 릴리즈 직전:
  - `S19` 체크리스트(`07`) 4항목 통과 여부 확인 후 결론 제출

## 6. 제출 템플릿
```text
[S19 Debt/Release Block]
- Build: <sha/tag>
- debt_total: <value>
- debt_burndown: <value%>
- overdue_count: <value>
- reopen_count: <value>
- blocked_status: <none/guarded/blocked>
- blocked_minutes: <value>
- Exception:
  - active: <count>
  - nearest_expiry: <YYYY-MM-DD HH:mm>
- Verdict: <go/guarded/blocked>
- Required actions:
  1) <action>
  2) <action>
  3) <action>
```

## 7. 운영 체크포인트
- [ ] debt 점수가 severity/상태/가중치 규칙과 일치하는가
- [ ] 차단/해제 상태가 임계치 기준에 따라 자동 반영되는가
- [ ] 예외 승인 필드/만료/재평가가 누락 없이 기록되는가
- [ ] `blocked` 상태에서 릴리즈 진행이 차단되는가

## 8. 문서 연동
- 보정 액션 폐쇄 루프: `47-freeze-drill-corrective-action-loop-spec.md`
- 드릴 준비도 점수: `46-freeze-drill-readiness-score-spec.md`
- 프리즈 지휘/커뮤니케이션: `45-freeze-command-and-communication-playbook.md`
- 위험예산/프리즈 정책: `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md` (`S19-1~S19-4`)
- 실행 보드: `10-detailed-todo-board.md` `BG` 트랙

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
