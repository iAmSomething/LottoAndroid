# 통합 결론 위험예산/프리즈 정책 스펙 (Cycle-57)

## 1. 목적
- `S14` 추세 결과를 실제 릴리즈 제어(진행/감속/프리즈) 정책으로 연결한다.
- 반복되는 조건부/보류 패턴을 방치하지 않고 위험예산 소진 기준으로 즉시 대응한다.

## 2. 운영 범위(S15)
| 항목 | 설명 | 필수 |
|---|---|---|
| R15-1 | 위험예산 정의: 주간 허용 조건부/보류 횟수 및 코드별 한도 | 예 |
| R15-2 | 프리즈 트리거: 예산 초과 시 릴리즈 동결/부분 동결 조건 | 예 |
| R15-3 | 해제 조건: 재측정/재검증 완료 후 프리즈 해제 기준 | 예 |
| R15-4 | 예외 승인 규칙: 제품 오너 승인 하 조건부 진행 허용 범위 | 예 |
| R15-5 | 주간 리스크 리포트: 예산 소진율 + 프리즈 상태 + 복구 ETA | 예 |

## 3. 위험예산 규칙
1. 주간 예산
- 조건부 진행 허용: 최대 2건
- 보류 허용: 0건(발생 즉시 프리즈 후보)

2. 코드별 한도
- 동일 에스컬레이션 코드(E13-*) 주간 2회 초과 시 `WARN`
- 동일 코드 3회 이상 시 해당 영역 릴리즈 `부분 프리즈`

3. 소진율 계산
- `risk_budget_used = (조건부 진행 수 * 0.5) + (보류 수 * 1.0)`
- `risk_budget_used >= 2.0`이면 `FAIL`

## 4. 프리즈/해제 정책
1. 부분 프리즈
- 트리거: 특정 코드 반복 3회 이상
- 조치: 해당 영역 기능 변경 병합 중지 + 회귀 원인 제거 우선

2. 전체 프리즈
- 트리거: `보류` 1건 이상 또는 주간 소진율 `FAIL`
- 조치: 릴리즈 파이프라인 일시 중지 + 원인 RCA 완료 전 재개 금지

3. 해제 조건
- 동일 코드 재발 0건(최소 2개 빌드 연속)
- `S10/S11/S12/S13` 재평가 PASS
- 액션 게이트 완료율 95% 이상

## 5. 예외 승인 규칙
- 승인 권한: 제품 오너 + 릴리즈 오너 공동 승인
- 허용 범위: 조건부 진행 1건 한정, 보류는 예외 승인 불가
- 승인 시 필수 기록:
  - 승인 사유 3줄
  - 영향 범위
  - 복구 ETA

## 6. 제출 템플릿
```md
# Unified Verdict Risk Budget <date>
- Week: <yyyy-ww>
- Conditional count: <n>
- Hold count: <n>
- Risk budget used: <x>
- Repeated escalation codes: <E13-*:count>
- Freeze status: <none/partial/global>
- Exception approval: <none/approved>
- Recovery ETA: <date/time>
- Next controls:
  1) <control 1>
  2) <control 2>
  3) <control 3>
```

## 7. 운영 체크포인트
- [ ] 주간 위험예산 소진율이 계산되어 기록됐는가
- [ ] 프리즈 트리거 발생 시 상태가 즉시 반영됐는가
- [ ] 예외 승인 기록이 필수 필드를 모두 포함하는가
- [ ] 프리즈 해제 조건 충족 여부가 체크리스트에 반영됐는가

## 8. 문서 연동
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 드라이런/에스컬레이션: `42-unified-verdict-dryrun-and-escalation-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `BC` 트랙

## 9. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S15` 프리즈 발동 이후 실행 절차는 `45-freeze-command-and-communication-playbook.md`에서 정의한다.

## 10. Cycle-59 프리즈 드릴/준비도 연동
- `S15` 정책의 실행 가능성 검증은 `46-freeze-drill-readiness-score-spec.md` 드릴 결과로 확인한다.

## 11. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S15` 프리즈 해제 후 보정 액션의 overdue/reopen 관리는 `47-freeze-drill-corrective-action-loop-spec.md` 규칙으로 통제한다.

## 12. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S15` 프리즈 운영과 병행되는 debt/blocked 판정은 `48-corrective-action-debt-and-release-block-spec.md` 규칙으로 통제한다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
