# 실기기 연결 즉시 실행 번들 명령 (`BK-001` -> `BK-002` -> `P-004`)

## 1. 목적
- 폰 실기기 1대 + Wear 실기기 2대(소형/대형)가 연결된 순간, 물리 게이트를 한 번에 실행하는 표준 명령을 고정한다.
- `BK-001` -> `BK-002` -> `P-004` 실행 순서와 완료 판정 기준을 문서 기준으로 통일한다.

## 2. 사전 조건
- `adb devices`에서 다음 조건이 충족되어야 한다.
  - 폰(비-watch) 실기기 1대 이상
  - Wear(`ro.build.characteristics`에 `watch`) 실기기 2대 이상
- 기본 실행 위치: 리포지토리 루트 (`/Volumes/무제/lotto`)

## 3. 즉시 실행 표준 명령 (원커맨드)
```bash
./scripts/run-all-physical-gates-when-ready.sh \
  --date-tag <yyyy-mm-dd> \
  --poll-interval 3 \
  --timeout-seconds 600 \
  --save-blocked-report
```

옵션:
- Wear 앱 재설치를 포함하려면 `--no-skip-wear-install` 추가
- 보고서 경로를 고정하려면 `--orchestrator-report`, `--checkpoint-report`, `--bk-decision-report`, `--wear-report` 지정

## 4. 내부 실행 순서 (고정)
1. `BK-001`: `run-bk-device-gate.sh` 내부에서 device profile 성능 측정 실행  
   (`run-performance-sample-check.sh --profile device --repeat 5 --warmup 1`)
2. `BK-002`: 동일 스크립트 내부에서 릴리즈 판정 실행  
   (`evaluate-performance-gate.sh`)
3. `P-004`: `run-p4-wear-proof-gate.sh`로 Wear 소형/대형 2대 실측 증적 수집
4. `checkpoint`: `run-physical-gates-checkpoint.sh`로 `BK/P-004` 통합 상태 동기화

## 5. 산출물
- `docs/assets/distribution/physical_gates_orchestrator_<date>.md`
- `docs/assets/distribution/physical_gates_checkpoint_<date>.md`
- `docs/assets/distribution/performance_release_decision_checkpoint_<date>.md`
- `docs/assets/distribution/wear_p4_device_evidence_checkpoint_<date>.md`

## 6. 완료 판정
- 커맨드 exit code 기준:
  - `0`: 완료 (`BK=PASS`, `P-004=PASS`, checkpoint PASS)
  - `2`: `BLOCKED` (실기기 연결 미충족 또는 타임아웃)
  - `1`: `FAIL` (스크립트/환경/게이트 실패)
- 문서 기준:
  - `physical_gates_checkpoint_<date>.md`에서
    - `BK gate (BK-001/BK-002): PASS`
    - `Wear P-004 gate: PASS`
  - 두 줄이 모두 `PASS`일 때만 물리 게이트 완료로 간주한다.

## 7. 완료 후 동기화 명령
```bash
./scripts/sync-physical-blockers-from-checkpoint.sh \
  --date-tag <yyyy-mm-dd> \
  --apply
```
- 위 동기화는 checkpoint가 `PASS`일 때만 `10-detailed-todo-board.md`의 `BK-001`, `BK-002`, `P-004`를 자동 완료 처리한다.

## 8. 운영 메모
- 실기기 미보유 상태에서 주간 운영은 `69`(루틴), `70`(요약 템플릿), `71`(장기화 리스크) 기준을 우선 적용한다.
- 본 문서는 "실기기 연결 즉시 실행" 기준이며, 주기 점검 루틴은 `run-physical-gates-routine-check.sh`를 사용한다.
