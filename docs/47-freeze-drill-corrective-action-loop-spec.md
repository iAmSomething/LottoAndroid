# 프리즈 드릴 보정 액션 폐쇄 루프 스펙 (Cycle-60)

## 1. 목적
- `S17` 드릴 결과에서 발생한 WARN/FAIL 항목을 "실행 가능한 보정 액션"으로 전환하고, 폐쇄까지 추적하는 표준 운영 루프를 정의한다.
- 릴리즈/품질/디자인 관점에서 후속 조치 누락을 방지하고 재발(reopen) 비율을 낮춘다.

## 2. 트리거와 범위
- 트리거:
  - 드릴 판정이 `WARN` 또는 `FAIL`
  - `S10`/`S11`/`S12` 결론에서 보정 액션이 생성된 경우
- 범위:
  - 프리즈 발동/해제/사후 회고에서 생성된 모든 액션
  - 성능/UI/운영 커뮤니케이션/외부 연동 fallback 이슈

## 3. 액션 레코드 필수 필드
- `action_id`: `CA-YYYYMMDD-###`
- `source`: `S17` | `S12` | `S13` | `S15` | `S16`
- `scenario`: `Drill-A` | `Drill-B` | `Drill-C` | `Release`
- `severity`: `P0` | `P1` | `P2`
- `owner`: 담당자
- `due_at`: 기한(로컬 타임존)
- `verify_gate`: 재검증 기준(`S10`, `S11`, `S12` 중 해당 게이트)
- `status`: `open` | `in_progress` | `blocked` | `closed` | `reopen`
- `evidence_path`: 증적 경로(로그/캡처/회의록)

## 4. SLA 기준
- `P0`: 24시간 이내 폐쇄
- `P1`: 72시간 이내 폐쇄
- `P2`: 7일 이내 폐쇄
- 공통:
  - 기한 초과 시 `overdue`로 자동 분류하고 다음 루틴 최상단 TODO로 승격
  - `P0` overdue는 프리즈 유지 여부 재평가를 즉시 요청

## 5. 폐쇄 게이트(C18)
- `C18-1` 재현 시나리오 재실행 PASS
- `C18-2` 관련 게이트 재평가 PASS(`S10` 또는 `S11`, 필요 시 `S12`)
- `C18-3` 증적 1건 이상 첨부(로그/스크린샷/리포트)
- `C18-4` 담당자 + 검토자 승인 기록
- `C18-5` 실행 보드(`10`)와 체크리스트(`07`) 상태 동기화

## 6. 재개방(Reopen) 규칙
- 폐쇄 후 7일 내 동일 원인 재발 시 `reopen`으로 전환
- `reopen` 전환 시:
  - 원인 유형(재현 누락/검증 누락/운영 누락) 분류
  - 동일 액션 재사용 대신 새 `action_id`를 발급하고 상호 링크
  - 다음 드릴 점수(`S17`)에서 패널티(-5점) 반영

## 7. 운영 리듬
- 일일:
  - `open/in_progress/blocked/overdue` 상태 점검
- 주간:
  - `closure_rate`, `reopen_rate`, `overdue_count` 집계
  - `S14` 추세 카드와 연동해 반복 취약 영역 클러스터링
- 릴리즈 직전:
  - `P0/P1` 미폐쇄 0건 확인 후 결론 제출

## 8. 보고 템플릿
```text
[S18 Corrective Action]
- Action ID: <CA-YYYYMMDD-###>
- Source/Scenario: <S17 / Drill-A|B|C>
- Severity: <P0|P1|P2>
- Owner/Due: <name> / <YYYY-MM-DD HH:mm>
- Verify gate: <S10|S11|S12>
- Status: <open|in_progress|blocked|closed|reopen>
- Evidence:
  1) <path>
  2) <path>
- Closure check:
  - C18-1: <PASS/FAIL>
  - C18-2: <PASS/FAIL>
  - C18-3: <PASS/FAIL>
  - C18-4: <PASS/FAIL>
  - C18-5: <PASS/FAIL>
- Notes: <optional>
```

## 9. 운영 체크포인트
- [ ] WARN/FAIL 액션이 필수 필드와 함께 등록됐는가
- [ ] SLA 위반 액션이 `overdue`로 분류되고 루틴 TODO로 승격됐는가
- [ ] 폐쇄 시 C18-1~C18-5가 모두 충족됐는가
- [ ] 재발 시 `reopen` 처리와 패널티 반영이 수행됐는가

## 10. 문서 연동
- 드릴/준비도 점수: `46-freeze-drill-readiness-score-spec.md`
- 프리즈 지휘/커뮤니케이션: `45-freeze-command-and-communication-playbook.md`
- 위험예산/프리즈 정책: `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md` (`S18-1~S18-4`)
- 실행 보드: `10-detailed-todo-board.md` `BF` 트랙

## 11. Cycle-63 부채/차단 정책 연동
- `S18` 폐쇄 루프의 debt 집계/차단·해제 규칙은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
