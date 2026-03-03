# Debt / Release Block Report (2026-W09)

## S19-Debt 산출(`S19-1`)
- 입력 액션:
  - P1 open/in_progress: 2건 (`2 * 5 = 10`)
  - P2 open: 1건 (`1 * 2 = 2`)
  - P2 reopen+overdue: 1건 (`2 + 2 + 3 = 7`)
- debt_total: `19`
- debt_burndown: `-11.8%` (전주 17 → 금주 19)
- 판정: 계산 규칙 일치

## 차단 규칙 검증(`S19-2`)
- 실제 케이스:
  - debt_total=19, P0 overdue=0
  - verdict=`guarded` (규칙 일치)
- 경계 초과 시뮬레이션:
  - debt_total=22 가정
  - verdict=`blocked` 전환 확인

## 해제 규칙 검증(`S19-3`)
- 해제 시뮬레이션:
  - 핵심 액션 2건 close + debt_total 11 회복
  - 재평가 회의 승인 기록 추가
  - 결과: `blocked -> guarded` 해제 가능
- 현재 운영 상태:
  - BK/BQ 미완료로 실운영 해제 조건 미충족

## 예외 승인 검증(`S19-4`)
- 예외 레코드:
  - reason: 실기기 수급 지연으로 72시간 한정 조건부 진행 요청
  - owner: release-owner
  - reviewer: product-owner
  - expires_at: 2026-03-01 12:00 KST
  - mitigation: BK-001 우선 실행 + 일일 리포트
- 만료 처리 검증:
  - expires_at 경과 시 자동 재평가 트리거
  - 해소 실패 시 즉시 `blocked` 재전환 규칙 확인

## 요약
- blocked_status: guarded
- overdue_count: 1
- reopen_count: 1
- exception_active_count: 1
- required actions:
  1) BK-001 실기기 device 성능 리포트
  2) BK-002 성능 판정 재평가
  3) BQ-002 핫패스 프로파일링 템플릿
