# 물리 게이트 블로커 상태 동기화 체크리스트 (`10`/`11`/`18`)

## 1. 목적
- 물리 게이트(`BK-001`, `BK-002`, `P-004`) 상태가 바뀌거나 `blocked` 증적이 갱신될 때, 핵심 문서 3종(`10`, `11`, `18`)의 상태 불일치를 방지한다.

## 2. 적용 시점
- `run-all-physical-gates-when-ready.sh` 실행 직후
- `run-physical-gates-checkpoint.sh` 실행 직후
- 주간 루틴(`run-physical-gates-routine-check.sh`) 결과 반영 시
- `sync-physical-blockers-from-checkpoint.sh --apply` 실행 직후

## 3. 입력 산출물(근거)
- `docs/assets/distribution/physical_gates_orchestrator_<date>.md`
- `docs/assets/distribution/physical_gates_checkpoint_<date>.md`
- `docs/assets/distribution/performance_release_decision_checkpoint_<date>.md`
- `docs/assets/distribution/wear_p4_device_evidence_checkpoint_<date>.md`

## 4. 동기화 체크리스트
- [ ] `10-detailed-todo-board.md`의 `P-004`, `BK-001`, `BK-002` 상태가 checkpoint 결과와 일치한다.
- [ ] `10-detailed-todo-board.md`의 블로커 증적 링크가 최신 `<date>` 파일을 가리킨다.
- [ ] `11-progress-tracker.md` 당일 항목에 실행 명령, 상태(`PASS/BLOCKED/FAIL`), 산출물 경로가 기록됐다.
- [ ] `18-device-validation-report.md` 실행 이력에 당일 상태와 근거 파일이 추가됐다.
- [ ] `10`/`11`/`18`에 기록된 상태 문자열(`PASS/BLOCKED/FAIL`)이 서로 동일하다.
- [ ] 날짜 태그(`<yyyy-mm-dd>`)가 `10`/`11`/`18`에서 동일하다.
- [ ] checkpoint가 `PASS`라면 `sync-physical-blockers-from-checkpoint.sh --apply` 실행 이력을 남겼다.

## 5. 상태 판정 규칙(단일 기준)
| 기준 파일 | 판정 키 | 허용 값 |
|---|---|---|
| `physical_gates_checkpoint_<date>.md` | `BK gate (BK-001/BK-002)` | `PASS` / `BLOCKED` / `FAIL` |
| `physical_gates_checkpoint_<date>.md` | `Wear P-004 gate` | `PASS` / `BLOCKED` / `FAIL` |
| `performance_release_decision_checkpoint_<date>.md` | `Release Status` | `READY*` / `PENDING` / `BLOCKED` |

운영 규칙:
1. checkpoint의 `BK gate`, `Wear P-004 gate`를 1차 소스로 사용한다.
2. `Release Status`는 보조 지표이며, blocker 닫힘 여부는 checkpoint 기준으로만 판정한다.
3. 상태 충돌 시 `10`/`11`/`18`을 먼저 수정하고, 수정 근거를 `11`에 남긴다.

## 6. 실행 순서(권장)
1. 물리 게이트 스크립트 실행
```bash
./scripts/run-all-physical-gates-when-ready.sh --date-tag <yyyy-mm-dd> --save-blocked-report
```
2. checkpoint 기준 자동 동기화(조건 충족 시)
```bash
./scripts/sync-physical-blockers-from-checkpoint.sh --date-tag <yyyy-mm-dd> --apply
```
3. `10` -> `11` -> `18` 순서로 문서 반영
4. 최종 diff에서 상태/날짜/증적 경로 일치 여부 확인 후 커밋
