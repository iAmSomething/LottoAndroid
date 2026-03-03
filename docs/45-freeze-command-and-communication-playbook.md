# 프리즈 지휘/커뮤니케이션 플레이북 (Cycle-58)

## 1. 목적
- `S15`에서 프리즈가 발동된 이후의 실행 편차를 줄이고, 대응 속도를 고정한다.
- "프리즈 발동은 했는데 누구에게 무엇을 공유하고 언제 해제 판단하는지"가 흔들리지 않도록 운영 절차를 표준화한다.

## 2. 운영 범위(S16)
| 항목 | 설명 | 필수 |
|---|---|---|
| C16-1 | 프리즈 지휘 체계(RACI): 의사결정/실행/검증/공유 책임 분리 | 예 |
| C16-2 | 커뮤니케이션 템플릿: 상태 공지/영향 범위/ETA/다음 체크포인트 | 예 |
| C16-3 | 복구 체크리스트: RCA/수정/재측정/재평가 순서 고정 | 예 |
| C16-4 | 해제 회의 기준: 해제 전 필수 입력물/결정 조건 표준화 | 예 |
| C16-5 | 사후 회고: 재발 방지 액션과 소유자/기한 확정 | 예 |

## 3. 프리즈 지휘 체계(RACI)
| 역할 | 책임 |
|---|---|
| 제품 오너 | 프리즈 유지/해제 최종 승인 |
| 릴리즈 오너 | 상태 공지, 체크포인트 운영, 의사결정 로그 관리 |
| 성능/디자인 오너 | 원인 분석(RCA), 수정안 제시, 재측정 보고 |
| QA 오너 | 재현 검증, 체크리스트 게이트 통과 여부 확인 |

## 4. 커뮤니케이션 규칙
1. 프리즈 발동 공지(즉시)
- 포함 필드: `원인 코드`, `영향 범위`, `현재 상태(partial/global)`, `다음 업데이트 시각`

2. 정기 업데이트(최대 2시간 간격)
- 포함 필드: 진행률, 차단 요인, ETA 변동, 필요한 의사결정

3. 해제 제안 공지
- 포함 필드: 재측정 결과, 게이트 통과 여부, 잔여 리스크, 권장 결론

## 5. 복구/해제 절차
1. RCA 기록 완료
2. 수정안 반영 후 `S10/S11/S12/S13/S15` 재평가
3. 체크리스트(`07`) `S15-3` 통과 확인
4. 해제 회의(15분 타임박스)에서 `진행/조건부/유지` 결정
5. 사후 회고 항목 3개 이내 확정

## 6. 사후 회고 규칙
- 재발 방지 액션은 최대 3개
- 모든 액션에 `owner`/`due date` 필수
- 다음 루틴의 최상단 TODO로 이관

## 7. 제출 템플릿
```md
# Freeze Command Log <date>
- Freeze type: <partial/global>
- Trigger: <reason>
- Scope: <affected area>
- Current owner: <release owner>
- Last update: <time>
- Recovery status:
  1) RCA: <done/pending>
  2) Re-test: <done/pending>
  3) Gate re-check: <pass/warn/fail>
- Unfreeze proposal: <진행/조건부 진행/유지>
- Postmortem actions:
  1) <action 1 / owner / due>
  2) <action 2 / owner / due>
  3) <action 3 / owner / due>
```

## 8. 운영 체크포인트
- [ ] 프리즈 발동 10분 내 첫 공지가 발행됐는가
- [ ] 2시간 이내 주기 업데이트가 유지됐는가
- [ ] 해제 제안에 필수 입력물(RCA/재측정/게이트 재평가)이 포함됐는가
- [ ] 사후 액션이 다음 루틴 TODO로 이관됐는가

## 9. 문서 연동
- 위험예산/프리즈 정책: `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`
- 통합 결론 이력/추세: `43-unified-verdict-history-and-trend-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `BD` 트랙

## 10. Cycle-59 프리즈 드릴/준비도 연동
- `S16` 플레이북의 현장 적용성은 `46-freeze-drill-readiness-score-spec.md` 점수카드로 주간 검증한다.

## 11. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S16` 운영 중 생성된 후속 액션의 책임자/기한/폐쇄 증적 관리는 `47-freeze-drill-corrective-action-loop-spec.md`를 따른다.

## 12. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S16` 운영 중 생성된 후속 액션의 debt/blocked 판정 및 예외 만료 관리는 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
