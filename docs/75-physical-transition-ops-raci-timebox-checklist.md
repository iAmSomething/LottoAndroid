# 실기기 전환 당일 운영 체크리스트 (RACI + 타임박스)

## 1. 목적
- 실기기 전환 당일(`BLOCKED` -> `PASS`) 실행에서 역할 공백 없이 의사결정과 실행 책임을 고정한다.
- Day-0 런북(`74`) 실행을 시간 박스 단위로 관리해 지연/누락을 줄인다.

## 2. 적용 범위
- 대상 게이트: `BK-001`, `BK-002`, `P-004`
- 기준 문서:
  - `74-physical-device-day0-transition-runbook.md`
  - `73-physical-blocker-state-sync-checklist.md`
  - `71-blocked-state-longtail-risk-criteria.md`

## 3. 역할 정의 (RACI)
| 작업 | Driver (R) | Accountable (A) | Consulted (C) | Informed (I) |
|---|---|---|---|---|
| 기기 준비/연결 확인 | `release-ops` | `qa-lead` | `android-lead` | `product-lead` |
| 오케스트레이터 실행 | `release-ops` | `qa-lead` | `android-lead` | `product-lead` |
| 실패 원인 분석/재실행 결정 | `android-lead` | `qa-lead` | `release-ops` | `product-lead` |
| 문서 동기화(`10`/`11`/`18`) | `release-ops` | `qa-lead` | `android-lead` | `product-lead` |
| `L3` 에스컬레이션 승인 | `qa-lead` | `product-lead` | `android-lead` | `release-ops` |

## 4. 타임박스 체크리스트 (당일)
| 단계 | 제한 시간 | 체크 항목 | 완료 기준 |
|---|---:|---|---|
| T0 사전 점검 | 10분 | `adb devices`, 배터리/디버깅, 날짜 태그 고정 | 폰 1대 + Wear 2대 확인 |
| T1 1차 실행 | 20분 | `run-all-physical-gates-when-ready.sh` 실행 | orchestrator + checkpoint 산출물 생성 |
| T2 동기화 | 10분 | `sync-physical-blockers-from-checkpoint.sh --apply` + `73` 체크리스트 점검 | `10`/`11`/`18` 정합성 일치 |
| T3 재시도 판단 | 10분 | `PASS/BLOCKED/FAIL` 판정 + 재실행 여부 결정 | 재실행 또는 당일 보류 결정 |
| T4 에스컬레이션 | 15분 | `L3` 조건 충족 시 승인/액션 등록 | owner/기한/SLA 명시 |

## 5. 운영 룰
1. T1 완료 전에는 문서 상태를 변경하지 않는다.
2. `BLOCKED`는 최대 1회 재시도 후 당일 보류 여부를 결정한다.
3. `FAIL`은 원인 복구 후 같은 `DATE_TAG`로 재실행한다.
4. `PASS`는 당일 안에 `10`/`11`/`18` 동기화를 반드시 완료한다.

## 6. 당일 종료 체크
- [ ] `physical_gates_checkpoint_<date>.md` 상태 확인 완료
- [ ] `10-detailed-todo-board.md` 상태 반영 완료
- [ ] `11-progress-tracker.md` 실행 로그 반영 완료
- [ ] `18-device-validation-report.md` 실행 이력 반영 완료
- [ ] 다음 액션(필요 시 최대 3개) 등록 완료
