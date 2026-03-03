# 통합 결론 이력/추세 관리 스펙 (Cycle-56)

## 1. 목적
- `S13` 드라이런 결과를 단건 기록으로 끝내지 않고, 주간 추세로 관리해 품질 리스크를 조기에 감지한다.
- 통합 결론(`진행/조건부 진행/보류`)의 반복 패턴을 추적해 다음 루틴 우선순위를 자동으로 정렬한다.

## 2. 운영 범위(S14)
| 항목 | 설명 | 필수 |
|---|---|---|
| H1 | 통합 결론 이력 레지스트리(빌드별 결론/규칙/에스컬레이션 코드) | 예 |
| H2 | 주간 추세 카드(진행/조건부/보류 비율, 리드타임, 누락률) | 예 |
| H3 | 반복 이슈 클러스터(동일 원인 2회 이상) 자동 식별 | 예 |
| H4 | 액션 이행률 대시보드(등록 대비 완료율) | 예 |
| H5 | 주간 리뷰/인수인계 템플릿(다음 주 우선순위 3개) | 예 |

## 3. 기록 규칙
1. 결론 이벤트 단위
- 빌드 1건당 통합 결론 레코드 1건 생성
- 필수 필드: `build_id`, `s10_verdict`, `s11_verdict`, `applied_rule`, `escalation_code`, `final_decision`, `lead_time_min`

2. 파일 규칙
- 경로: `docs/assets/distribution/unified_verdict_<yyyy-mm-dd>.md`
- 주간 요약: `docs/assets/distribution/unified_verdict_weekly_<yyyy-ww>.md`

3. 누락 처리
- 필수 필드 누락 시 해당 레코드는 `invalid`로 분류
- `invalid` 발생 시 다음 루틴 `S14` 항목에 보정 액션 1건 의무 등록

## 4. 추세 판정 규칙
1. 조건부/보류 비율
- 최근 7일 기준 `조건부+보류` 비율 30% 초과 시 `WARN`
- 40% 초과 시 `FAIL`

2. 리드타임
- 평균 리드타임 20분 초과 시 `WARN`
- 30분 초과 시 `FAIL`

3. 액션 이행률
- 액션 완료율 90% 미만 시 `WARN`
- 80% 미만 시 `FAIL`

4. 반복 이슈
- 동일 에스컬레이션 코드(E13-*)가 2주 내 3회 이상이면 원인 클러스터를 `P1`로 승격

## 5. 주간 리뷰 절차
1. `S14` 레지스트리 업데이트
2. 주간 추세 카드 산출(H2)
3. 반복 이슈 클러스터 확인(H3)
4. 다음 루틴 우선순위 3개 확정(H5)
5. 체크리스트/백로그 반영

## 6. 제출 템플릿
```md
# Unified Verdict Weekly Review <yyyy-ww>
- Total builds: <n>
- Final decisions: 진행 <n> / 조건부 <n> / 보류 <n>
- Decision risk ratio(조건부+보류): <x>%
- Avg lead time(min): <x>
- Action completion rate: <x>%
- Repeated escalation codes: <E13-1:x, E13-2:y ...>
- Weekly verdict: <PASS/WARN/FAIL>
- Next priorities:
  1) <우선순위 1>
  2) <우선순위 2>
  3) <우선순위 3>
```

## 7. 운영 체크포인트
- [ ] 최근 7일 빌드 결론 레코드가 누락 없이 기록됐는가
- [ ] 추세 카드와 원본 레코드 수가 일치하는가
- [ ] 반복 이슈 클러스터가 백로그(`10`, `16`)에 반영됐는가
- [ ] 주간 우선순위 3개가 다음 루틴 시작점과 정합한가

## 8. 문서 연동
- 통합 결론 패키지: `41-unified-quality-verdict-package-spec.md`
- 드라이런/에스컬레이션: `42-unified-verdict-dryrun-and-escalation-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `BB` 트랙

## 9. Cycle-57 위험예산/프리즈 연동
- `S14` 추세 결과의 릴리즈 제어 단계는 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`에서 정의한다.

## 10. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S14` 추세 결과로 프리즈가 발동되면 `45-freeze-command-and-communication-playbook.md` 절차를 따른다.

## 11. Cycle-59 프리즈 드릴/준비도 연동
- `S14` 추세 결과의 운영 준비도 점검은 `46-freeze-drill-readiness-score-spec.md`를 따른다.

## 12. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S14` 주간 추세에 closure_rate/reopen_rate/overdue_count 지표를 포함할 때는 `47-freeze-drill-corrective-action-loop-spec.md` 기준을 따른다.

## 13. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S14` 주간 추세에 debt_total/debt_burndown/blocked_minutes 지표를 포함할 때는 `48-corrective-action-debt-and-release-block-spec.md` 기준을 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
