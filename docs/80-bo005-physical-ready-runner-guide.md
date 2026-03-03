# BO-005 실기기 준비 대기 러너 가이드

## 1. 목적
- `BO-005`(실기기 PASS 기반 실배포 경로 1회 성공) 항목을 실기기 연결 시점에 즉시 재시도할 수 있도록 대기-실행 러너를 표준화한다.
- 실기기/체크포인트 준비가 안 된 상태에서는 timeout 기반 blocked 리포트를 남기고 종료한다.

## 2. 스크립트
- `./scripts/run-bo005-when-physical-ready.sh`

## 3. 준비 조건
- `physical_gates_checkpoint_<date>.md`가 존재하고 아래 2개가 모두 `PASS`
  - `BK gate (BK-001/BK-002): PASS`
  - `Wear P-004 gate: PASS`
- ADB 기준 실기기 1대 연결(또는 `--serial` 지정)
- Firebase 파라미터 준비(`project-id`, `app-id`, `groups`, `service-account`)

## 4. 실행 예시
```bash
./scripts/run-bo005-when-physical-ready.sh \
  --date-tag <yyyy-mm-dd> \
  --checkpoint-report docs/assets/distribution/physical_gates_checkpoint_<yyyy-mm-dd>.md \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --groups <tester-group-alias> \
  --service-account <service-account-json-path> \
  --poll-interval 30 \
  --timeout-seconds 1800
```

## 5. 출력 파일
- 러너 요약: `docs/assets/distribution/bo005_when_physical_ready_<date>.md`
- 체인 리포트: `docs/assets/distribution/firebase_physical_pass_chain_bo005_<date>.md`

## 6. 판정
- 성공: 러너 리포트 `result=PASS` + 체인 리포트 `status=PASS`
- 차단: 러너 리포트 `result=BLOCKED_TIMEOUT`
- 실패: 러너 리포트 `result=FAIL`(체인 exit code non-zero)

## 7. 운영 메모
- 다중 실기기 환경에서는 `--serial`을 명시한다.
- 체인 스크립트(`run-physical-pass-firebase-chain.sh`)는 `--serial` 옵션을 지원한다.
