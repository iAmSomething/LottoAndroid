# Capacity / Coverage Report (2026-02-26)

- Window: 2026-02-26 20:00~23:30 KST

## 커버리지 검증(`S21-1`)
- primary_oncall: release-owner
- secondary_oncall: qa-owner
- reviewer: product-owner
- 빈 슬롯: 0건
- primary/secondary 동일인 배치: 없음
- 판정: PASS

## 포화 전환 검증(`S21-2`)
- 입력:
  - 동시 L2+ 경보: 2건
  - queue_depth: 3
  - blocked_minutes: 132
- 상태 전환:
  - `normal -> saturated` 전환
  - 우선순위 재정렬(`P0 > P1 > P2`) 적용
- 판정: 규칙 일치 PASS

## 핸드오버 검증(`S21-3`)
- 전달 항목:
  - 미해결 경보 2건(level/owner/sla_remaining)
  - guarded 원인 2건
  - 예외 승인 만료 시각 1건
- 수락 로그: 기록됨
- handover_loss: 0
- 판정: PASS

## 회복 검증(`S21-4`)
- 회복 조건 체크:
  - queue_depth: 1 (충족)
  - L2/L3 경보: 0건 (충족)
  - overdue 증가: 없음 (충족)
- 상태 전환:
  - `saturated -> recovered -> normal`
- recovery_time: 42분
- recovery report publish: 24분 내 발행(PASS)

## Next actions
1. BQ-001 실기기 수급/연결 체크리스트 확정.
2. BK-001 실기기 성능 수집 시작.
3. BQ-003 공지 자동화 스크립트 초안 작성.
