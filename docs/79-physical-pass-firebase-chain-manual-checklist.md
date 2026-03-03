# BO-001 수동 실행 체크리스트(실행 전/후)

## 1. 목적
- `run-physical-pass-firebase-chain.sh`를 사람이 수동 실행할 때 누락 없이 준비/검증/기록하도록 표준 체크리스트를 고정한다.
- 실기기 PASS 이후 배포 체인의 `PASS/FAIL` 판정을 일관되게 남긴다.

## 2. 적용 범위
- 대상 스크립트: `./scripts/run-physical-pass-firebase-chain.sh`
- 기본 출력 리포트: `docs/assets/distribution/firebase_physical_pass_chain_<yyyy-mm-dd>.md`
- 참조 런북: `77-firebase-distribution-after-physical-pass-runbook.md`

## 3. 실행 전 체크리스트 (Pre-Run)

### 3-1. 게이트/증적 준비
- [ ] `physical_gates_checkpoint_<date>.md` 파일이 존재한다.
- [ ] checkpoint 파일에 `BK gate (BK-001/BK-002): PASS`가 표시된다.
- [ ] checkpoint 파일에 `Wear P-004 gate: PASS`가 표시된다.

### 3-2. 실행 파라미터 준비
- [ ] `--project-id` 값을 확인했다.
- [ ] `--app-id` 값을 확인했다.
- [ ] `--groups`(tester group alias) 값을 확인했다.
- [ ] `--service-account` 경로에 JSON 파일이 실제로 존재한다.
- [ ] 필요 시 `--release-notes` 값을 준비했다.
- [ ] 기본 경로 외 출력이 필요하면 `--report-file` 경로를 지정했다.

### 3-3. 실행 환경 준비
- [ ] 작업 브랜치/커밋 상태를 확인하고 실행 로그에 기록할 준비를 했다.
- [ ] 실배포 목적이면 `--skip-final-check`를 사용하지 않는다.
- [ ] 리허설 목적이면 `--dry-run-only` 사용 여부를 사전에 명시했다.

## 4. 실행 명령 템플릿

### 4-1. 기본(권장: final-check + dry-run + 실배포)
```bash
./scripts/run-physical-pass-firebase-chain.sh \
  --date-tag <yyyy-mm-dd> \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --groups <tester-group-alias> \
  --service-account <service-account-json-path>
```

### 4-2. dry-run-only 리허설
```bash
./scripts/run-physical-pass-firebase-chain.sh \
  --date-tag <yyyy-mm-dd> \
  --project-id <firebase-project-id> \
  --app-id <firebase-app-id> \
  --groups <tester-group-alias> \
  --service-account <service-account-json-path> \
  --dry-run-only
```

## 5. 실행 후 체크리스트 (Post-Run)

### 5-1. 콘솔/종료코드 확인
- [ ] 스크립트 종료코드가 `0`이다.
- [ ] 출력 로그에 `[PASS] physical PASS -> firebase chain completed.`가 보인다.

### 5-2. 체인 리포트 확인
- [ ] `firebase_physical_pass_chain_<date>.md`가 생성되었다.
- [ ] `Step Results`의 `final_check` 값이 의도한 모드와 일치한다.
- [ ] `Step Results`의 `firebase_dry_run` 값이 `PASS`다.
- [ ] 실배포 모드라면 `firebase_distribute=PASS`다.
- [ ] 리허설 모드라면 `firebase_distribute=SKIPPED_DRY_RUN_ONLY`다.
- [ ] `Overall.status`가 기대한 결과(`PASS` 또는 실패 원인 확인)와 일치한다.

### 5-3. 문서 기록
- [ ] `11-progress-tracker.md`에 실행 명령/결과/리포트 경로를 기록했다.
- [ ] `18-device-validation-report.md`에 당일 실기기 PASS + 체인 결과를 기록했다.
- [ ] 필요 시 `docs/assets/distribution/`에 추가 증적(로그/CI run id)을 저장했다.

## 6. 실패 시 즉시 조치
1. `status=FAIL`이면 체인 리포트에서 최초 실패 단계를 확인한다.
2. 파라미터/시크릿/기기 상태를 수정한 뒤 같은 `date_tag` 기준으로 재실행한다.
3. `--skip-final-check` 사용 시 사유를 `11-progress-tracker.md`에 반드시 기록한다.

## 7. 참고 문서
- `77-firebase-distribution-after-physical-pass-runbook.md`
- `78-firebase-physical-pass-chain-report-template-guide.md`
- `14-signing-and-distribution.md`
