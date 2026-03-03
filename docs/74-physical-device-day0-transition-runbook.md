# 실기기 전환 Day-0 런북

## 1. 목적
- 실기기(폰 1대 + Wear 2대)가 준비된 당일, `BLOCKED` 상태를 `PASS`로 전환하기 위한 실행 순서를 고정한다.
- `BK-001`, `BK-002`, `P-004`를 같은 날짜 태그로 묶어 증적/문서 동기화 누락을 방지한다.

## 2. 실행 전 체크 (5분)
- [ ] `adb devices`에서 폰 1대(비-watch), Wear 2대(`watch`)가 모두 `device` 상태인지 확인
- [ ] 배터리 30% 이상, USB 디버깅 유지, 절전 모드 비활성화
- [ ] `DATE_TAG=$(date +%F)`로 당일 태그 고정

```bash
adb devices
DATE_TAG=$(date +%F)
echo "$DATE_TAG"
```

## 3. 표준 실행 순서
1. 물리 게이트 오케스트레이터 실행
```bash
./scripts/run-all-physical-gates-when-ready.sh \
  --date-tag "$DATE_TAG" \
  --save-blocked-report
```

2. checkpoint 기반 TODO 자동 동기화
```bash
./scripts/sync-physical-blockers-from-checkpoint.sh \
  --date-tag "$DATE_TAG" \
  --apply
```

3. 문서 동기화 체크리스트 수행
```bash
# 체크리스트 기준 문서
# docs/73-physical-blocker-state-sync-checklist.md
# docs/75-physical-transition-ops-raci-timebox-checklist.md
# docs/76-p4-evidence-screenshot-log-review-checklist.md
# docs/77-firebase-distribution-after-physical-pass-runbook.md
```

## 4. 성공 판정
- `physical_gates_checkpoint_<DATE_TAG>.md`에서 아래 두 줄이 모두 `PASS`
  - `BK gate (BK-001/BK-002): PASS`
  - `Wear P-004 gate: PASS`
- `docs/10-detailed-todo-board.md`에서 `P-004`, `BK-001`, `BK-002`가 `[x]` 반영됨

## 5. 실패/블로커 분기
- `exit=2` 또는 `BLOCKED`:
  - 실기기 연결 상태 점검 후 1회 재실행
  - 재실행도 `BLOCKED`면 당일은 증적 갱신만 유지하고 다음 슬롯 예약
- `exit=1` 또는 `FAIL`:
  - 스크립트/ADB/설치 실패 원인 우선 복구
  - 복구 후 동일 `DATE_TAG`로 재실행

## 6. 실행 후 기록(필수)
- `docs/11-progress-tracker.md` 당일 항목에 명령/결과/산출물 경로 추가
- `docs/18-device-validation-report.md` 실행 이력 갱신
- `docs/10-detailed-todo-board.md` 상태 확인 (`[!]` -> `[x]`)

## 7. 산출물 묶음
- `docs/assets/distribution/physical_gates_orchestrator_<DATE_TAG>.md`
- `docs/assets/distribution/physical_gates_checkpoint_<DATE_TAG>.md`
- `docs/assets/distribution/performance_release_decision_checkpoint_<DATE_TAG>.md`
- `docs/assets/distribution/wear_p4_device_evidence_checkpoint_<DATE_TAG>.md`
