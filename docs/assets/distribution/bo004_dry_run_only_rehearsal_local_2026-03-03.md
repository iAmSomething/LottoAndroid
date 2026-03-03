# BO-004 dry-run-only 리허설 증적 (로컬)

- 실행 일시: 2026-03-03 14:26:06 +0900
- 목표: `run-physical-pass-firebase-chain.sh`의 dry-run-only 경로 1회 검증
- 실행 환경: local

## 입력
- checkpoint: `docs/assets/distribution/physical_gates_checkpoint_bo004_rehearsal_2026-03-03.md` (시뮬레이션 PASS)
- project_id: `lottoeveryday`
- app_id: `1:1083851357764:android:2da8bc877b0e7c89b94611`
- groups: `suyeoni`
- options: `--skip-final-check --dry-run-only`

## 실행 명령
```bash
./scripts/run-physical-pass-firebase-chain.sh \
  --date-tag 2026-03-03 \
  --checkpoint-report docs/assets/distribution/physical_gates_checkpoint_bo004_rehearsal_2026-03-03.md \
  --project-id lottoeveryday \
  --app-id 1:1083851357764:android:2da8bc877b0e7c89b94611 \
  --groups suyeoni \
  --service-account ./lottoeveryday-firebase-adminsdk-fbsvc-06db76153d.json \
  --release-notes "BO-004 dry-run-only rehearsal (2026-03-03)" \
  --report-file docs/assets/distribution/firebase_physical_pass_chain_dry_run_only_rehearsal_2026-03-03.md \
  --skip-final-check \
  --dry-run-only
```

## 결과 요약
- `final_check=SKIPPED`
- `firebase_dry_run=PASS`
- `firebase_distribute=SKIPPED_DRY_RUN_ONLY`
- `status=PASS`

## 산출물
- 체인 리포트: `docs/assets/distribution/firebase_physical_pass_chain_dry_run_only_rehearsal_2026-03-03.md`
- 리허설 checkpoint: `docs/assets/distribution/physical_gates_checkpoint_bo004_rehearsal_2026-03-03.md`

## 주의
- 본 증적은 dry-run-only 리허설 증적이며 실배포 성공 증적이 아니다.
- BO-005는 실기기 실측 PASS checkpoint 기준 실배포 경로 실행 이력으로 별도 확보해야 한다.
