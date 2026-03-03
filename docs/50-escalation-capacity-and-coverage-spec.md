# 에스컬레이션 대응 용량/커버리지 스펙 (Cycle-65)

## 1. 목적
- `S20` 경보 체계를 실제 운영 가능한 대응 용량 체계로 연결한다.
- 경보 공백 시간, 동시 경보 포화, 교대 핸드오버 누락으로 인한 대응 실패를 줄인다.

## 2. 커버리지 매트릭스(S21-Coverage)
- 기본 구조:
  - `primary_oncall`: 현재 대응 책임자
  - `secondary_oncall`: 백업 대응자
  - `reviewer`: 해제/예외 승인 검토자
- 시간대 구간:
  - 평일 06:00~24:00: primary/secondary 필수
  - 비가동 시간: secondary + 긴급 호출 체계 유지
- 필수 규칙:
  - 빈 슬롯(미배정) 0건
  - primary/secondary 동일인 배치 금지

## 3. 포화(saturated) 전환 규칙
- 전환 조건:
  - 동시 `L2+` 경보 2건 이상
  - `queue_depth >= 3`
  - `blocked_minutes`가 90분 이상 누적
- 전환 시 동작:
  - 신규 경보 우선순위 재정렬(`P0 > P1 > P2`)
  - 비핵심 작업 중단 및 대응 전용 슬롯 확보
  - 릴리즈 결론 상태를 기본 `guarded`로 상향

## 4. 핸드오버 체크리스트
- 교대 시 필수 전달:
  - 미해결 경보 목록(`anomaly_level`, `owner`, `sla_remaining`)
  - `blocked/guarded` 상태 원인
  - 예외 승인 유효시간(`expires_at`)
- 체크포인트:
  - 인수자 수락 로그
  - 누락 항목 0건 확인

## 5. 회복(recovered) 규칙
- 회복 조건:
  - `queue_depth <= 1`
  - `L2/L3` 경보 0건
  - `overdue_count` 증가 없음(최근 1사이클)
- 회복 후 조치:
  - 상태를 `normal`로 전환
  - 회복 리드타임(`recovery_time`) 기록
  - 반복 원인 1건 이상을 다음 루틴 TODO로 등록

## 6. 응답 SLA
- `primary ack`: 10분 이내
- `secondary join`: 20분 이내(포화 시 10분 이내)
- `handover completion`: 교대 시작 후 15분 이내
- `recovery report publish`: 회복 후 30분 이내

## 7. 제출 템플릿
```text
[S21 Capacity/Coverage]
- Window: <YYYY-MM-DD HH:mm~HH:mm>
- Coverage:
  - primary: <name>
  - secondary: <name>
  - reviewer: <name>
- State: <normal|saturated|recovered>
- Queue:
  - queue_depth: <value>
  - unresolved_L2plus: <value>
- SLA:
  - primary ack: <PASS/FAIL>
  - secondary join: <PASS/FAIL>
  - handover completion: <PASS/FAIL>
  - recovery publish: <PASS/FAIL>
- Handover loss: <count>
- Recovery time(min): <value>
- Next actions:
  1) <action>
  2) <action>
  3) <action>
```

## 8. 운영 체크포인트
- [ ] 커버리지 매트릭스 빈 슬롯이 없는가
- [ ] 포화 전환 조건이 규칙대로 적용되는가
- [ ] 핸드오버 체크리스트 누락이 없는가
- [ ] 회복 후 지표(`queue_depth`, `handover_loss`, `recovery_time`)가 기록되는가

## 9. 문서 연동
- 이상징후/자동 에스컬레이션: `49-corrective-action-debt-anomaly-and-escalation-spec.md`
- 부채/차단 정책: `48-corrective-action-debt-and-release-block-spec.md`
- 보정 액션 폐쇄 루프: `47-freeze-drill-corrective-action-loop-spec.md`
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md` (`S21-1~S21-4`)
- 실행 보드: `10-detailed-todo-board.md` `BJ` 트랙
