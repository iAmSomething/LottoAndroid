# 실기기 PASS 이후 Firebase Distribution 연계 런북

## 1. 목적
- 실기기 전환 완료(`BK-001/BK-002/P-004` PASS) 직후 Firebase Distribution 실행과 판정을 한 흐름으로 고정한다.
- 배포 실행 조건, 성공/실패 판정, 후속 문서 기록 지점을 누락 없이 연결한다.

## 2. 선행 조건 (필수)
- `physical_gates_checkpoint_<date>.md`에서 아래 두 항목이 모두 `PASS`
  - `BK gate (BK-001/BK-002): PASS`
  - `Wear P-004 gate: PASS`
- `sync-physical-blockers-from-checkpoint.sh --apply` 실행으로 `10` 상태 반영 완료
- 릴리즈 서명/Firebase 시크릿 준비 완료(`14-signing-and-distribution.md` 기준)

## 3. 실행 순서
0. (권장) 체인 래퍼로 일괄 실행
```bash
./scripts/run-physical-pass-firebase-chain.sh \
  --date-tag <yyyy-mm-dd> \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --groups <tester-group-alias> \
  --service-account <service-account-json-path>
```

1. 실기기 강제 최종 점검
```bash
./scripts/release-final-check.sh --require-physical-device
```

2. Firebase 배포 전 dry-run 검증
```bash
./scripts/firebase-distribute.sh \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --service-account <service-account-json-path> \
  --groups <tester-group-alias> \
  --dry-run
```

3. Firebase 실제 배포 실행
```bash
./scripts/firebase-distribute.sh \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --service-account <service-account-json-path> \
  --groups <tester-group-alias>
```

## 4. 성공/실패 판정
- 성공:
  - `release-final-check` 요약 `FAIL=0`
  - `firebase-distribute.sh` 출력에 `[PASS] Firebase distribution completed.` 확인
- 실패:
  - 점검 실패(`release-final-check FAIL>0`) 또는 배포 실패(스크립트 exit non-zero)
  - 실패 시 실제 배포를 중단하고 원인 복구 후 동일 `date-tag` 기준 재실행

## 5. CI 연계 판정 (선택)
- `main` 머지 후 GitHub Actions의 `Firebase Distribution` 워크플로우 성공 여부를 최종 확인한다.
- 주기 점검 필요 시 `firebase-distribution-routine-check.sh`로 dry-run 증적을 추가한다.

## 6. 실행 후 기록 (필수)
- [ ] `11-progress-tracker.md`에 실행 명령/결과(run id 또는 로컬 결과) 기록
- [ ] `18-device-validation-report.md`에 당일 실기기 PASS + 배포 실행 결과 기록
- [ ] 필요 시 `docs/assets/distribution/`에 배포 증적 리포트 파일 추가
- [ ] `firebase_physical_pass_chain_<date>.md`는 `78-firebase-physical-pass-chain-report-template-guide.md` 기준으로 해석/판정

## 7. 표준 기록 포맷 (예시)
```md
- 2026-03-03: physical gates PASS 확인(`physical_gates_checkpoint_2026-03-03.md`)
- 2026-03-03: `./scripts/release-final-check.sh --require-physical-device` PASS
- 2026-03-03: `./scripts/firebase-distribute.sh ... --dry-run` PASS
- 2026-03-03: `./scripts/firebase-distribute.sh ...` PASS (Firebase Distribution 성공)
```
