# 실기기 부재 Blocked 증적 주간 갱신 루틴

## 1. 목적
- 실기기 부재 상태에서도 `P-004`(Wear 2대 실측)와 `BK`(폰 실기기 성능 판정)의 운영 상태를 주간 단위로 최신화한다.
- `blocked` 상태를 실패로 혼동하지 않고, 증적 파일 기준으로 상태를 추적한다.

## 2. 적용 범위
- TODO 보드: `P-004`, `BM-001`
- 물리 게이트: `BK-001`, `BK-002`, `P-004`
- 증적 경로: `docs/assets/distribution/*`

## 3. 실행 캘린더 (고정)
| 구분 | 고정 시각 | 실행 경로 | 담당 |
|---|---|---|---|
| 자동 주간 갱신 | 매주 월요일 11:00 KST (`cron: 0 2 * * 1`) | `.github/workflows/physical-gates-routine.yml` | release-ops |
| 수동 보정 실행 | 필요 시 즉시 | `scripts/run-physical-gates-routine-check.sh` | on-call |

## 4. 표준 실행 명령
```bash
./scripts/run-physical-gates-routine-check.sh \
  --date-tag <yyyy-mm-dd> \
  --timeout-seconds 1 \
  --poll-interval 1
```

## 5. 필수 산출물
- `docs/assets/distribution/physical_gates_orchestrator_<date>.md`
- `docs/assets/distribution/physical_gates_checkpoint_<date>.md`
- `docs/assets/distribution/performance_release_decision_checkpoint_<date>.md`
- `docs/assets/distribution/wear_p4_device_evidence_checkpoint_<date>.md`

## 6. 운영 판정 규칙
- `PASS`: 실기기 조건 충족, `sync-physical-blockers-from-checkpoint.sh --apply` 실행 가능
- `BLOCKED`: 실기기 미연결 상태, 증적 파일 갱신 후 TODO 상태 유지
- `FAIL`: 스크립트/환경 오류 우선 복구(ADB/권한/실행경로 확인)

## 7. 실행 후 동기화
1. `docs/10-detailed-todo-board.md`의 `P-004` 링크를 최신 증적으로 갱신
2. `docs/18-device-validation-report.md` 실행 이력에 당일 결과 추가
3. `docs/11-progress-tracker.md` 당일 완료 작업에 실행 명령/산출물 기록

## 8. 장기화 판정
- 연속 `BLOCKED` 주차가 2주 이상이면 `71-blocked-state-longtail-risk-criteria.md` 기준으로 레벨(`L2+`)을 판정한다.
