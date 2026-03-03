# 프리즈 드릴/준비도 점수 스펙 (Cycle-59)

## 1. 목적
- `S16` 플레이북을 실제로 실행 가능한 상태로 검증하기 위해 주기적인 프리즈 드릴을 운영한다.
- 드릴 결과를 점수화해 "해제 가능/보류 유지" 판단을 정량 기준으로 고정한다.

## 2. 운영 범위(S17)
| 항목 | 설명 | 필수 |
|---|---|---|
| D17-1 | 드릴 시나리오 세트(부분 프리즈/전체 프리즈/해제 회의) | 예 |
| D17-2 | 준비도 점수카드(지휘, 공지, 복구, 해제, 사후회고) | 예 |
| D17-3 | 합격 기준(점수 임계치) + 미달 시 보정 액션 | 예 |
| D17-4 | 드릴 증적 패키지(로그/회의기록/액션 이관) | 예 |
| D17-5 | 주간 운영 주기와 책임자 고정 | 예 |

## 3. 드릴 시나리오
1. Drill-A (부분 프리즈)
- 조건: 특정 E13 코드 3회 누적 가정
- 목표: 10분 내 발동 공지 + 영향 범위 확정

2. Drill-B (전체 프리즈)
- 조건: 보류 1건 발생 가정
- 목표: 파이프라인 중지/복구 계획/ETA 공유

3. Drill-C (해제 회의)
- 조건: 재측정 PASS 가정
- 목표: 15분 타임박스 내 `진행/조건부/유지` 결론 확정

## 4. 준비도 점수카드
- 총점 100점
- 지휘 체계 준수: 20점
- 공지/업데이트 시간 준수: 20점
- RCA/재측정 증적 완비: 20점
- 해제 회의 품질(입력물/결론): 20점
- 사후 액션 이관/owner/due 명확성: 20점

## 5. 합격 기준
- 85점 이상: `PASS`
- 70~84점: `WARN` (다음 루틴 보정 액션 2개 필수)
- 69점 이하: `FAIL` (프리즈 해제 승인 금지, 보정 후 재드릴)

## 6. 운영 주기
- 주 1회 최소 1개 시나리오 실행
- 릴리즈 주간은 Drill-A + Drill-C 필수
- 책임자: 릴리즈 오너(운영), QA 오너(검증), 제품 오너(승인)

## 7. 제출 템플릿
```md
# Freeze Drill Scorecard <date>
- Scenario: <A/B/C>
- Freeze type: <partial/global>
- Readiness score: <0-100>
- Verdict: <PASS/WARN/FAIL>
- Time SLA:
  - first notice: <min>
  - update interval: <ok/not ok>
  - unfreeze meeting: <min>
- Evidence:
  1) <log path>
  2) <meeting note path>
  3) <action transfer path>
- Next actions:
  1) <action 1>
  2) <action 2>
  3) <action 3>
```

## 8. 운영 체크포인트
- [ ] 드릴 시나리오/결론/점수가 기록됐는가
- [ ] SLA 위반 항목이 보정 액션으로 등록됐는가
- [ ] WARN/FAIL 시 액션이 다음 루틴 최상단에 이관됐는가
- [ ] 드릴 증적 패키지가 릴리즈 체크리스트와 연결됐는가

## 9. 문서 연동
- 프리즈 지휘/커뮤니케이션: `45-freeze-command-and-communication-playbook.md`
- 위험예산/프리즈 정책: `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `BE` 트랙

## 10. Cycle-60 보정 액션 폐쇄 루프 연동
- 드릴 결과로 생성되는 WARN/FAIL 후속 액션의 운영은 `47-freeze-drill-corrective-action-loop-spec.md`를 따른다.

## 11. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- 드릴 결과 후속 액션의 debt 점수 및 차단/해제 정책은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
