# Blocked 상태 장기화 리스크 기준

## 1. 목적
- 실기기 부재로 `P-004`/`BK`가 장기간 `BLOCKED`일 때, 운영 판단을 주차 기준으로 일관되게 수행한다.
- "증적 갱신은 되고 있으나 실제 해소가 지연되는 상태"를 별도 리스크로 관리한다.

## 2. 적용 범위
- 대상 게이트: `BK-001`, `BK-002`, `P-004`
- 입력 문서:
  - `69-physical-blocked-evidence-weekly-routine.md`
  - `70-physical-gates-weekly-summary-template.md`
  - `docs/assets/distribution/physical_gates_weekly_summary_<yyyy-w##>.md`

## 3. 핵심 지표
- `consecutive_blocked_weeks`: 연속 `BLOCKED` 주차 수
- `blocked_scope`: `BK only` | `P-004 only` | `BK + P-004`
- `evidence_freshness_hours`: 최근 증적 갱신 후 경과 시간

## 4. 임계 주차 기준
| 레벨 | 조건 | 상태 | 필수 액션 |
|---|---|---|---|
| L0 | `consecutive_blocked_weeks = 0` | 정상 | 정기 루틴 유지 |
| L1 | `consecutive_blocked_weeks = 1` | 주의 | 다음 주 실행 슬롯/장비 확보 계획 등록 |
| L2 | `consecutive_blocked_weeks = 2~3` | 경고 | 주간 리뷰에 고정 안건 승격 + 담당/기한 강제 |
| L3 | `consecutive_blocked_weeks >= 4` | 에스컬레이션 | release hold 검토 회의(15분) + 복구 계획 승인 필수 |

## 5. 에스컬레이션 조건
- 다음 중 하나라도 충족하면 레벨을 1단계 상향한다.
  1. `BK + P-004` 동시 `BLOCKED`가 2주 연속
  2. 주간 증적 파일 누락 또는 `evidence_freshness_hours > 168`
  3. 다음 액션(최대 3개) 중 완료 항목이 2주 연속 0건

## 6. 담당자(RACI)
| 역할 | 담당 |
|---|---|
| Driver | `release-ops` |
| Co-owner | `qa-lead` |
| Reviewer | `android-lead` |
| Approver (`L3`) | `product-lead` |

## 7. SLA
- 주간 요약 생성: 매주 월요일 18:00 KST 이전
- `L2` 이상 액션 등록: 판정 후 24시간 이내
- `L3` 회의 소집: 판정 후 48시간 이내

## 8. 해소 조건
- `BK`와 `P-004`가 같은 주차에 모두 `PASS`인 증적이 생성되면 `L0`로 복귀한다.
- 부분 해소(`BK PASS`, `P-004 BLOCKED` 등)는 레벨 유지 후 `blocked_scope`만 갱신한다.
