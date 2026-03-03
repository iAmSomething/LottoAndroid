# `firebase_physical_pass_chain_<date>.md` 템플릿/해석 가이드

## 1. 목적
- `run-physical-pass-firebase-chain.sh`가 생성하는 체인 리포트 형식을 고정한다.
- 결과 해석 기준을 통일해 `PASS/FAIL` 판정과 후속 조치를 빠르게 결정한다.

## 2. 출력 파일 규칙
- 기본 경로: `docs/assets/distribution/firebase_physical_pass_chain_<yyyy-mm-dd>.md`
- 사용자 지정: `--report-file <path>` 옵션으로 변경 가능

## 3. 표준 템플릿
```md
# Firebase Physical PASS Chain Report

- 실행 시각: <yyyy-mm-dd hh:mm:ss +0900>
- date_tag: <yyyy-mm-dd>
- checkpoint_report: <path>
- project_id: <firebase-project-id>
- app_id: <firebase-app-id>
- groups: <tester-group-aliases>

## Step Results
- final_check: <PASS|FAIL|SKIPPED>
- firebase_dry_run: <PASS|FAIL|PENDING>
- firebase_distribute: <PASS|FAIL|PENDING|SKIPPED_DRY_RUN_ONLY>

## Overall
- status: <PASS|FAIL>
```

## 4. 필드 해석 기준
- `final_check`
  - `PASS`: `release-final-check --require-physical-device` 통과
  - `SKIPPED`: `--skip-final-check` 옵션으로 생략
  - `FAIL`: 실기기 강제 점검 실패
- `firebase_dry_run`
  - `PASS`: Firebase CLI 배포 명령 dry-run 성공
  - `FAIL`: dry-run 단계 실패(파라미터/인증/APK 경로 등)
  - `PENDING`: 상위 단계 실패로 dry-run 미실행
- `firebase_distribute`
  - `PASS`: Firebase 실제 업로드 성공
  - `SKIPPED_DRY_RUN_ONLY`: `--dry-run-only` 실행으로 실배포 생략
  - `FAIL`: 실제 업로드 실패
  - `PENDING`: 상위 단계 실패로 미실행
- `status`
  - `PASS`: 체인 전체 성공
  - `FAIL`: 하나 이상의 필수 단계 실패

## 5. 운영 판정 규칙
1. `status=PASS`이면 배포 체인 성공으로 기록한다.
2. `status=FAIL`이면 `Step Results`에서 최초 실패 단계를 원인 구간으로 본다.
3. `firebase_distribute=SKIPPED_DRY_RUN_ONLY`는 정상 리허설 성공으로 분류하되, 실배포 완료로 간주하지 않는다.
4. `final_check=SKIPPED` 사용 시 반드시 사유를 `11-progress-tracker.md`에 남긴다.

## 6. 후속 기록 체크리스트
- [ ] `11-progress-tracker.md`에 실행 명령/결과/리포트 경로 기록
- [ ] `18-device-validation-report.md`에 실기기 PASS + 체인 결과 기록
- [ ] 필요 시 `14-signing-and-distribution.md`에 운영 메모 업데이트

## 7. 참고 문서
- `77-firebase-distribution-after-physical-pass-runbook.md`
- `14-signing-and-distribution.md`
- `18-device-validation-report.md`
