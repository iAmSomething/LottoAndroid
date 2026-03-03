# 릴리즈 체크리스트

## 1. 빌드/품질
- [x] `./gradlew :app:assembleRelease`
- [x] `./gradlew :app:testDebugUnitTest`
- [x] `./gradlew :app:ktlintCheck :app:detekt`
- [x] `./gradlew :app:connectedDebugAndroidTest` (실기기/에뮬레이터 필수)
- [x] 실기기 재검증 자동화 명령 준비(`./scripts/run-physical-device-validation.sh`)
- [x] 프리플라이트 실기기 엄격 모드 추가(`./scripts/release-preflight.sh --with-build --require-physical-device`)
- [x] 배포 전 최종 점검 래퍼 추가(`./scripts/release-final-check.sh`)
- [x] 크래시 재현 케이스 점검 (계측 테스트 재실행 + 수동 instrumentation 확인)
- [x] `./scripts/release-preflight.sh --with-build` 실행 및 리포트 저장(`17-release-preflight-report.md`)
- [x] CI 프리플라이트 워크플로우 추가(`.github/workflows/release-preflight.yml`)
- [x] stats CTA 로그 샘플 검증 루틴 추가(`./scripts/run-analytics-sample-check.sh`, `verify-analytics-events --profile stats-cta`)
- [x] Firebase 배포 주기 점검 워크플로우 추가(`.github/workflows/firebase-distribution-routine.yml`, dry-run 체인)
- [x] Firebase 배포 주기 점검 첫 CI 실행 증적 확보(run `22436650122`, artifact `firebase_routine_ci_22436650122.md`)
- [x] 운영 관측성 샘플 검증 루틴 추가(`./scripts/run-ops-observability-check.sh`, `verify-analytics-events --profile ops-core`)
- [x] 운영 관측성 임계치 자동 판정 루틴 추가(`./scripts/evaluate-ops-observability-threshold.sh`, `run-ops-observability-check --threshold-report`)
- [x] 크래시/ANR 자동 분류 템플릿 루틴 추가(`./scripts/classify-crash-anr-template.sh --log-file <path> --report-file <path>`)
- [x] 릴리즈 위험 점수 자동 산출 루틴 추가(`./scripts/calculate-release-risk-score.sh`, `.github/workflows/release-risk-score.yml`)
- [x] 시크릿 파일 정책 자동 가드 추가(`./scripts/check-secret-file-policy.sh`, `.github/workflows/secret-policy-guard.yml`)

## 2. 기능 확인
- [x] 번호 생성/잠금/저장 (단위 + 계측 스모크)
- [x] QR 등록(정상/오류) (파서 단위 + QR 수동입력 계측)
- [x] 당첨 조회/채점/하이라이트 (채점 단위 + 결과 화면 계측 진입)
- [x] 알림 예약/취소/재예약 (SettingsViewModel 단위 저장/재설정)
- [x] 위젯 A/B 표시 (WidgetDataProvider 단위 스냅샷)
- [x] 통계 지표 표시 (StatsViewModel 단위 계산/기간필터)

## 3. 스토어 배포 준비
- [x] 앱 설명 문구 초안 작성 (`12-store-metadata.md`)
- [x] 앱 아이콘/스크린샷 최종본 준비 (`docs/assets/store-screenshots/*.png`)
- [x] adaptive launcher icon 최종본 반영(`ic_launcher_foreground/background/monochrome`)
- [x] 버전코드/버전명 업데이트 (`versionCode=2`, `versionName=0.2.0`)
- [x] 개인정보/권한 고지 초안 작성 (`13-privacy-and-permissions.md`)
- [x] 서명/배포 절차 문서화 (`14-signing-and-distribution.md`)
- [x] 서명 키/릴리즈 키 보관 확인 (로컬 키 + 백업 생성, `17-release-preflight-report.md`)

## 4. 운영 인수인계
- [x] 문서 00~11 최신화
- [x] 릴리즈 문서 12~14 추가
- [x] 장애 대응 루틴 공유 (`15-incident-response-runbook.md`)
- [x] 다음 스프린트 백로그 정리 (`16-next-sprint-backlog.md`)

## 5. 2026-02-25 사전 점검 결과
- API 36 AVD에서 `connectedDebugAndroidTest` 9/9 통과(재실행 포함 검증)
- `ktlintCheck`, `detekt`, `testDebugUnitTest`, `assembleDebug`, `assembleRelease`는 통과
- 배포 직전에는 실제 기기 1대 이상에서 계측 테스트를 1회 추가 검증 권장

## 6. 자동 검증 근거(추가)
- `connectedDebugAndroidTest`: 9/9 통과
- 신규 계측: `MainNavigationInstrumentedTest`, `QrManualFlowInstrumentedTest`
- 신규 단위: `SettingsViewModelTest`, `DefaultWidgetDataProviderTest`, `StatsViewModelTest`, `BallChipAccessibilityTest`, `TicketDetailShareFormatterTest`, `ColorContrastTest`
- 당첨 API fallback: 공식 API 실패 시 미러 API로 1212회 로드 확인
- CI preflight 모드(`--with-build-ci --skip-adb --require-signing`) 검증 통과

## 7. 최종 배포 직전 실행 명령
1. 공통(자동 대상 선택: 실기기 우선, 없으면 에뮬레이터)
```bash
./scripts/release-final-check.sh
```
- 통과 조건: 요약 `FAIL=0`
- 실기기 없을 때는 에뮬레이터 검증으로 진행되며, 실기기 검증은 `pending`으로 남김
- ADB 디바이스가 전혀 없으면 CI-only fallback(`--with-build-ci --skip-adb --require-signing`)으로 자동 전환
- Wear `P-004` probe를 자동 실행해 2종 실기기 부재 시 blocked 리포트를 갱신 (`wear_p4_device_evidence_release_probe_<date>.md`)

2. 스토어 제출 직전(실기기 필수 강제)
```bash
./scripts/release-final-check.sh --require-physical-device
```
- 통과 조건: 실기기 1대 이상 연결 + 요약 `FAIL=0`
- 다중 실기기 연결 시:
```bash
./scripts/release-final-check.sh --require-physical-device --serial <adb-serial>
```

3. 물리 게이트 상태 체크포인트(`BK-001/002 + P-004`)
```bash
./scripts/run-physical-gates-checkpoint.sh --date-tag <yyyy-mm-dd>
```
- 결과가 `BLOCKED`이면 실기기 연결 전까지 릴리즈 진행 보류
- 통합 리포트: `docs/assets/distribution/physical_gates_checkpoint_<date>.md`

4. 물리 게이트 대기형 오케스트레이션(실기기 연결 시 자동 완료)
```bash
./scripts/run-all-physical-gates-when-ready.sh --date-tag <yyyy-mm-dd> --save-blocked-report
```
- 폰 실기기 + Wear 2종 연결을 대기한 뒤 `BK-001/002`, `P-004`, 체크포인트를 순차 실행
- 오케스트레이터 리포트: `docs/assets/distribution/physical_gates_orchestrator_<date>.md`

5. 체크포인트 기반 블로커 상태 동기화
```bash
./scripts/sync-physical-blockers-from-checkpoint.sh --date-tag <yyyy-mm-dd> --apply
```
- `physical_gates_checkpoint_<date>.md`가 PASS일 때만 `10-detailed-todo-board.md`의 `P-004`, `BK-001`, `BK-002`를 자동 완료 처리

6. 물리 게이트 루틴 점검(주기 실행/증적 아카이브)
```bash
./scripts/run-physical-gates-routine-check.sh --date-tag <yyyy-mm-dd> --report-file docs/assets/distribution/physical_gates_routine_<yyyy-mm-dd>.md
```
- `BLOCKED`는 실패가 아닌 운영 상태로 간주하며 증적 리포트 생성이 목표
- GitHub Actions: `.github/workflows/physical-gates-routine.yml`

## 8. 하드닝 게이트(예정)
- [x] `S01` 오류 매핑 표준 검증: timeout/4xx/5xx/schema/storage/external_open_failed 분기별 메시지/재시도/로그 필드 확인 (`docs/assets/distribution/hardening_gate_s01_a05_2026-02-26.md`)
- [x] `A05` fallback 공통 컴포넌트 검증: 외부 이동 실패 시 `링크 복사/기본 브라우저` 동작 회귀 확인 (`ExternalOpenFallbackDialog`, `HomeScreen`, 증적 문서 동일)
- [x] `S02` 성능 샘플 검증: 콜드스타트 P95, 주요 화면 jank, ANR 지표 수집 리포트 생성
  - 최신 실행: `docs/assets/distribution/performance_gate_emulator_2026-02-26.md` (N=5, warm-up 1, median/P95 기록)
  - 실행 증적 패키지: `docs/assets/distribution/performance_gate_execution_2026-02-26.md`

## 9. 유스케이스 리허설/핫패스 성능 게이트(예정)
- [x] `S05` 여정 리허설 검증: `J01~J06` 중 최소 4개 재현 + 복구 경로 확인 + 증적 저장 (`usecase_rehearsal_s05_2026-02-26.md`, J01~J04 재현)
- [x] `S05` 실패 복구 UX 검증: 오류 안내 1초 이내 노출, fallback 2탭 이내 도달 (`SettingsPurchaseRedirectInstrumentedTest`, `ExternalOpenFallbackDialogInstrumentedTest`)
- [x] `S06` 핫패스 성능 검증: Home/Result/Manage 우선 구간에서 startup/render/jank 기준 충족 (`hotpath_s06_profile_2026-02-26.md`, `performance_gate_emulator_s06_2026-02-26.md`, `HotpathRenderLatencyInstrumentedTest`)
- [x] `S04` 자동 수집 연동 검증: 성능 샘플 리포트를 릴리즈 판정 입력으로 연결 (`release-preflight.sh` 성능 섹션 자동 실행 + 판정 리포트 생성)
- [x] `P-004` Wear 실기기 증적 자동화: 2종(소형/대형) 증적 게이트/대기형 스크립트 및 blocked 리포트 경로 고정 (`run-p4-wear-proof-gate.sh`, `run-p4-when-wear-physical.sh`, `wear_p4_device_evidence_blocked_2026-02-26.md`)

## 10. 구현 슬라이스/롤백 게이트(예정)
- [x] `S07-1` 네트워크 오류 매핑 적용 검증: timeout/4xx/5xx/schema/unknown 키/메시지/로그 일관성 확인 (`AppErrorCategory`, `DrawApiClient`, `ResultErrorUi`, `docs/assets/distribution/hardening_gate_s07_1_s07_2_2026-02-26.md`)
- [x] `S07-2` 저장소 오류 매핑 적용 검증: storage_full/disk_io/migration 분기별 안내/재시도 정책 확인 (`RoomTicketRepository` error_type 분류, `ResultErrorUi` 분기, 증적 문서 동일)
- [x] `S07-3` fallback 공통 컴포넌트 검증: Home 외 화면(Settings) 확장 시 동작/계측 회귀 없음 확인 (`SettingsScreen` 확장 + `SettingsPurchaseRedirectInstrumentedTest` 통과)
- [x] `S07-4` 성능 수집 파이프라인 검증: startup/jank/ANR 리포트 자동 생성 및 체크리스트 연동 확인 (`run-performance-sample-check.sh`, `release-preflight.sh`, `performance_gate_execution_2026-02-26.md`)
- [x] `S07-5` 롤백 규칙 검증: P0/P1 임계치 초과 시 배포 보류 판정 로그 기록 (`evaluate-performance-gate.sh`, `performance_release_decision_simulated_hold_2026-02-26.md`)

## 11. 성능 게이트 캘리브레이션(예정)
- [x] `S08-1` 프로파일 분리 검증: `emulator`/`device` 측정 결과를 별도 리포트로 생성 (`performance_gate_emulator_2026-02-26.md`, `performance_gate_device_2026-02-26.md`)
- [x] `S08-2` 반복측정 검증: 최소 N=5(1회 warm-up 제외) 집계가 리포트에 기록되는지 확인 (`Runs: total=6, warmup=1, measured=5`)
- [x] `S08-3` 판정 규칙 검증: emulator는 baseline 대비, device는 절대 임계치 기준으로 판정 (`run-performance-sample-check.sh` profile 분기)
- [x] `S08-4` 릴리즈 차단 규칙 검증: `device FAIL`일 때만 보류 판정이 활성화되는지 확인 (`performance_release_decision_simulated_hold_2026-02-26.md` + 실제 리포트는 `PENDING_DEVICE_VALIDATION`)

## 12. 성능 게이트 운영 템플릿(예정)
- [x] `S09-1` 실행 커맨드 검증: emulator/device 프로파일 명령이 동일 옵션 체계로 실행되는지 확인 (`performance_gate_execution_2026-02-26.md` 1장)
- [x] `S09-2` 리포트 템플릿 검증: 필수 필드(Device, Runs, median/P95, Verdict)가 누락 없이 기록되는지 확인 (`performance_gate_execution_2026-02-26.md` 2장)
- [x] `S09-3` 판정 트리 검증: `device FAIL -> 보류`, `device PASS + emulator FAIL -> 최적화 백로그 등록` 규칙 적용 확인 (`performance_release_decision_2026-02-26.md`, `performance_release_decision_simulated_hold_2026-02-26.md`)
- [x] `S09-4` 후속 연결 검증: WARN/FAIL 원인이 `10-detailed-todo-board.md` 다음 루틴 항목으로 연결되는지 확인 (`P-004` 연동, `performance_gate_execution_2026-02-26.md` 5장)

## 13. 성능 게이트 증적 패키지(예정)
- [x] `S10-1` 증적 패키지 검증: E1~E5(emulator/device 리포트, 판정 카드, 결론, 후속 액션) 완비 여부 확인 (`performance_gate_evidence_2026-02-26.md`)
- [x] `S10-2` 제출 템플릿 검증: 최종 결론(진행/조건부 진행/보류)과 근거 3줄 기록 확인 (`performance_gate_evidence_2026-02-26.md`)
- [x] `S10-3` 규칙 일치 검증: 결론이 `S09` 판정 트리와 모순 없이 일치하는지 확인 (`performance_release_decision_2026-02-26.md` 기반 `보류` 결론)
- [x] `S10-4` 후속 연결 검증: 보류/조건부 진행 사유가 다음 루틴 TODO로 등록되는지 확인 (`10-detailed-todo-board.md` `BK` 트랙)

## 14. UI 품질 게이트(예정)
- [x] `S11-1` 타이포 위계 검증: Home/Result/Manage에서 숫자/단위/본문 위계가 1.0x/1.3x 모두 유지되는지 확인 (`ui_quality_gate_evidence_2026-02-26.md`, `docs/assets/visual-proof-matrix/*_normal_1_0_b.png`, `*_normal_1_3_b.png`)
- [x] `S11-2` 비주얼 일관성 검증: 카드 레이어/CTA 강조/아이콘 규격이 화면 4종에서 일관적인지 확인 (`ui_quality_gate_evidence_2026-02-26.md`, `32-visual-proof-matrix-report.md`)
- [x] `S11-3` 상호작용 안정성 검증: press feedback/시트 전환/fallback 3액션이 응답 예산 내 동작하는지 확인 (`ManageFilterSheetInstrumentedTest`, `SettingsPurchaseRedirectInstrumentedTest`, `ExternalOpenFallbackDialogInstrumentedTest`, `ui_quality_gate_2026-02-26.log`)
- [x] `S11-4` 판정 일치 검증: `S10` 성능 결론과 `S11` UI 결론이 모순 없이 동일 결론으로 수렴하는지 확인 (`ui_quality_gate_evidence_2026-02-26.md` 최종 `보류`)

## 15. 통합 품질 결론 패키지(예정)
- [x] `S12-1` 입력 일치 검증: `S10`/`S11` 결론이 동일 빌드 기준으로 생성됐는지 확인 (`performance_gate_evidence_2026-02-26.md`, `ui_quality_gate_evidence_2026-02-26.md`, build `7fb46eb`)
- [x] `S12-2` 충돌 해소 규칙 검증: 결론 충돌 시 R1~R4 적용 결과가 명시됐는지 확인 (`unified_verdict_2026-02-26.md`, rule `R1`)
- [x] `S12-3` 단일 결론 검증: 최종 결론(진행/조건부 진행/보류)이 체크리스트 상태와 일치하는지 확인 (`unified_verdict_2026-02-26.md`, 최종 `보류`)
- [x] `S12-4` 후속 실행 검증: 즉시 실행 액션(최대 3개)이 다음 루틴 TODO로 등록됐는지 확인 (`10-detailed-todo-board.md` `BK-001~BK-003`)

## 16. 통합 결론 드라이런/에스컬레이션(예정)
- [x] `S13-1` 드라이런 검증: 최근 2개 빌드 기준 `S10/S11` 통합 결론 카드가 생성됐는지 확인 (`unified_verdict_dryrun_2026-02-26.md`, Build-A/B)
- [x] `S13-2` 충돌 리허설 검증: 결론 충돌 케이스 최소 1건에 대해 규칙 적용 결과가 기록됐는지 확인 (`unified_verdict_dryrun_2026-02-26.md`, Rehearsal-C1, R2)
- [x] `S13-3` 에스컬레이션 검증: E13 코드(E13-1~E13-4)가 조건별로 누락 없이 기록됐는지 확인 (`unified_verdict_dryrun_2026-02-26.md`, D3)
- [x] `S13-4` SLA 검증: 드라이런/충돌 해소/보류 TODO 등록 시간이 SLA(15/20/10분) 내인지 확인 (`unified_verdict_dryrun_2026-02-26.md`, D4 PASS)

## 17. 통합 결론 이력/추세 운영(예정)
- [x] `S14-1` 이력 레지스트리 검증: 최근 7일 빌드 결론 레코드가 누락 없이 기록됐는지 확인 (`unified_verdict_weekly_2026-w09.md`, H1)
- [x] `S14-2` 추세 카드 검증: 조건부/보류 비율, 리드타임, 액션 이행률 계산이 원본 레코드와 일치하는지 확인 (`unified_verdict_weekly_2026-w09.md`, H2)
- [x] `S14-3` 반복 이슈 검증: 동일 E13 코드 반복 발생 시 클러스터가 백로그에 승격됐는지 확인 (`unified_verdict_weekly_2026-w09.md`, H3 + `10-detailed-todo-board.md` BK 트랙 유지)
- [x] `S14-4` 주간 리뷰 검증: 다음 루틴 우선순위 3개가 주간 리포트에 기록됐는지 확인 (`unified_verdict_weekly_2026-w09.md`, H5)

## 18. 통합 결론 위험예산/프리즈 정책(예정)
- [x] `S15-1` 위험예산 검증: 주간 조건부/보류 카운트와 소진율(`risk_budget_used`) 계산이 정확한지 확인 (`unified_verdict_risk_budget_2026-w09.md`, 산식 2.0)
- [x] `S15-2` 프리즈 트리거 검증: 부분/전체 프리즈 조건 발생 시 상태가 즉시 반영되는지 확인 (`unified_verdict_risk_budget_2026-w09.md`, global freeze)
- [x] `S15-3` 해제 조건 검증: 2개 빌드 연속 재발 0건 + 게이트 재평가 PASS 여부 확인 (`unified_verdict_risk_budget_2026-w09.md`, 미충족 판정 기록)
- [x] `S15-4` 예외 승인 검증: 공동 승인/사유/영향/ETA 기록이 누락 없이 남는지 확인 (`unified_verdict_risk_budget_2026-w09.md`, 예외 승인 샘플 필드 점검)

## 19. 프리즈 지휘/커뮤니케이션 운영(예정)
- [x] `S16-1` 지휘 체계 검증: 프리즈 시 RACI(제품/릴리즈/성능·디자인/QA) 역할이 명확히 기록되는지 확인 (`freeze_command_log_2026-02-26.md`, RACI 섹션)
- [x] `S16-2` 공지 규칙 검증: 발동 후 10분 내 첫 공지, 2시간 내 주기 업데이트가 유지되는지 확인 (`freeze_command_log_2026-02-26.md`, timeline PASS)
- [x] `S16-3` 해제 회의 검증: RCA/재측정/게이트 재평가 입력물로 15분 타임박스 회의가 수행되는지 확인 (`freeze_command_log_2026-02-26.md`, Recovery status)
- [x] `S16-4` 사후 회고 검증: 재발 방지 액션(최대 3개)에 owner/due가 기록되고 다음 루틴 TODO로 이관되는지 확인 (`freeze_command_log_2026-02-26.md`, BQ-001~BQ-003)

## 20. 프리즈 드릴/준비도 점수 운영(예정)
- [x] `S17-1` 드릴 실행 검증: Drill-A/B/C 중 최소 1개가 실행되고 증적이 저장됐는지 확인 (`freeze_drill_scorecard_2026-02-26.md`, Drill-B)
- [x] `S17-2` 점수카드 검증: 준비도 점수(100점 기준)와 PASS/WARN/FAIL 판정이 계산 규칙과 일치하는지 확인 (`freeze_drill_scorecard_2026-02-26.md`, 82점 WARN)
- [x] `S17-3` SLA 검증: 발동 공지/업데이트/해제 회의 시간이 기준 내인지 확인 (`freeze_drill_scorecard_2026-02-26.md`, SLA 섹션 PASS)
- [x] `S17-4` 후속 조치 검증: WARN/FAIL의 보정 액션이 다음 루틴 TODO로 이관됐는지 확인 (`10-detailed-todo-board.md`, `BQ-001~BQ-003`)

## 21. 드릴 보정 액션 폐쇄 루프 운영(예정)
- [x] `S18-1` 액션 등록 검증: WARN/FAIL 항목이 severity/owner/due/검증게이트와 함께 누락 없이 등록됐는지 확인 (`corrective_action_loop_2026-w09.md`, action registry)
- [x] `S18-2` SLA 준수 검증: P0/P1/P2 액션의 기한 준수율과 overdue 집계가 정확한지 확인 (`corrective_action_loop_2026-w09.md`, SLA 점검)
- [x] `S18-3` 폐쇄 게이트 검증: 재현 시나리오 재실행 PASS + 증적 첨부 + 승인 로그가 모두 충족됐는지 확인 (`corrective_action_loop_2026-w09.md`, C18 샘플)
- [x] `S18-4` 재개방/패널티 검증: 재발 시 액션이 reopen 처리되고 다음 드릴 점수에 패널티가 반영됐는지 확인 (`corrective_action_loop_2026-w09.md`, reopen/penalty)

## 22. 보정 액션 부채/릴리즈 차단 운영(예정)
- [x] `S19-1` 부채 산출 검증: severity 가중치 기반 debt 점수와 burn-down 계산이 규칙과 일치하는지 확인 (`debt_release_block_2026-w09.md`, debt_total=19)
- [x] `S19-2` 차단 규칙 검증: debt 임계치 초과 시 릴리즈 상태가 `blocked`로 즉시 전환되는지 확인 (`debt_release_block_2026-w09.md`, threshold 시뮬레이션)
- [x] `S19-3` 해제 규칙 검증: 핵심 액션 폐쇄 + debt 임계치 이하 회복 시 차단 해제가 정확히 반영되는지 확인 (`debt_release_block_2026-w09.md`, unblock 시나리오)
- [x] `S19-4` 예외 승인 검증: 예외 승인(사유/만료/책임자) 기록과 만료 후 자동 재평가가 누락 없이 수행되는지 확인 (`debt_release_block_2026-w09.md`, exception/expiry)

## 23. 보정 액션 부채 이상징후/자동 에스컬레이션 운영(예정)
- [x] `S20-1` 이상징후 탐지 검증: debt 급증/blocked 장기화/overdue 폭증 조건이 규칙대로 탐지되는지 확인 (`anomaly_escalation_2026-02-26.md`, L2/blocked_long)
- [x] `S20-2` 경보 단계 검증: L1/L2/L3 경보 단계와 대상 채널이 조건별로 정확히 발행되는지 확인 (`anomaly_escalation_2026-02-26.md`, release-ops+product-lead)
- [x] `S20-3` 응답 SLA 검증: 경보 수신 후 ack/owner 지정/초기 대응 시간이 기준 내인지 확인 (`anomaly_escalation_2026-02-26.md`, SLA PASS)
- [x] `S20-4` 사후 반영 검증: 에스컬레이션 결과가 `S14` 추세/`S19` debt 지표/다음 루틴 TODO에 동기화되는지 확인 (`anomaly_escalation_2026-02-26.md`, sync done)

## 24. 에스컬레이션 대응 용량/커버리지 운영(예정)
- [x] `S21-1` 커버리지 검증: 시간대별 primary/secondary 온콜 매트릭스가 빈 슬롯 없이 유지되는지 확인 (`capacity_coverage_2026-02-26.md`, 빈 슬롯 0)
- [x] `S21-2` 용량 검증: 동시 경보 처리 한도 초과 시 포화 상태(`saturated`) 전환과 대응 우선순위가 적용되는지 확인 (`capacity_coverage_2026-02-26.md`, normal->saturated)
- [x] `S21-3` 핸드오버 검증: 교대 시 미해결 경보/owner/SLA 잔여 시간이 체크리스트로 이관되는지 확인 (`capacity_coverage_2026-02-26.md`, handover_loss=0)
- [x] `S21-4` 회복 검증: 포화 해소 후 정상 운영 복귀와 지표(`queue_depth`, `handover_loss`)가 기록되는지 확인 (`capacity_coverage_2026-02-26.md`, recovered/normal)

## 25. UI 미감/타이포 완성도 게이트(예정)
- [x] `S22-1` A1~A5 scorecard 템플릿 검증: 타이포 개성/시각 대비/공백 리듬/모션 강약/외부 이동 신뢰 UX 기준이 문서화됐는지 확인 (`51-visual-aesthetic-and-typography-hardening-spec.md`)
- [x] `S22-2` 증적 팩 규격 검증: 전후 캡처/저조도/폰트스케일/상호작용 영상/판정 로그 필수 산출물 정의 여부 확인 (`51` 7장)
- [x] `S22-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S22` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S22-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S22` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 26. 상태 기반 UI 내러티브/마이크로카피 게이트(예정)
- [x] `S23-1` N1~N5 scorecard 템플릿 검증: 첫 인상/상태 일관성/마이크로카피 톤/모션 리듬/외부 이동 신뢰 기준이 문서화됐는지 확인 (`52-stateful-ui-narrative-and-microcopy-hardening-spec.md`)
- [x] `S23-2` 상태 매트릭스 증적 규격 검증: loading/empty/error/offline/redirect 전후 캡처/로그 필수 산출물 정의 여부 확인 (`52` 7장)
- [x] `S23-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S23` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S23-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S23` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 27. 상태 전환 코레오그래피/피드백 아이덴티티 게이트(예정)
- [x] `S24-1` C1~C5 scorecard 템플릿 검증: 전환 연속성/피드백 계층/완급/접근성 동등성/복구 명확성 기준이 문서화됐는지 확인 (`53-state-transition-choreography-and-feedback-identity-spec.md`)
- [x] `S24-2` 전환 증적 팩 규격 검증: enter/steady/exit 전환 캡처/로그와 feedback layer 필수 산출물 정의 여부 확인 (`53` 7장)
- [x] `S24-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S24` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S24-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S24` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 28. 적응형 레이아웃/시선 흐름 게이트(예정)
- [x] `S25-1` L1~L5 scorecard 템플릿 검증: 첫 시선 도달성/레이아웃 밀도/엄지 영역 접근성/CTA 우선순위/폰트스케일 안정성 기준이 문서화됐는지 확인 (`54-adaptive-layout-and-attention-flow-gate-spec.md`)
- [x] `S25-2` 화면 크기별 증적 팩 규격 검증: 소형/표준/대형 전후 캡처와 CTA 발견성 로그 필수 산출물 정의 여부 확인 (`54` 7장)
- [x] `S25-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S25` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S25-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S25` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 29. 시각 자산 일관성/로딩 fallback 게이트(예정)
- [x] `S26-1` V1~V5 scorecard 템플릿 검증: 자산 스타일 일관성/로딩/실패 fallback/대비 가독성/성능 예산 기준이 문서화됐는지 확인 (`55-visual-asset-consistency-and-fallback-gate-spec.md`)
- [x] `S26-2` 자산 증적 팩 규격 검증: 아이콘/이미지/배지 전후 캡처와 로딩 실패 fallback 로그 필수 산출물 정의 여부 확인 (`55` 7장)
- [x] `S26-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S26` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S26-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S26` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 30. 컨텍스트 우선순위/집중 모드 게이트(예정)
- [x] `S27-1` F1~F5 scorecard 템플릿 검증: 핵심 정보 우선순위/집중 모드 노이즈 억제/CTA 재배치/복귀 흐름/접근성 동등성 기준이 문서화됐는지 확인 (`56-context-priority-and-focus-mode-gate-spec.md`)
- [x] `S27-2` 집중 모드 증적 팩 규격 검증: 기본/집중 모드 전후 캡처와 알림 억제/복귀 로그 필수 산출물 정의 여부 확인 (`56` 7장)
- [x] `S27-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S27` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S27-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S27` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 31. 상호작용 신뢰 신호/실행 확인 게이트(예정)
- [x] `S28-1` R1~R5 scorecard 템플릿 검증: 즉시 반응/확정 상태/완료 확인/실패 복구/중복 입력 억제 기준이 문서화됐는지 확인 (`57-interaction-trust-signal-and-confirmed-state-gate-spec.md`)
- [x] `S28-2` confirmed state 증적 팩 규격 검증: 탭 직후 상태 변화/완료 피드백/오류 복구 로그 필수 산출물 정의 여부 확인 (`57` 7장)
- [x] `S28-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S28` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S28-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S28` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 32. 의사결정 신뢰도/점진적 공개 UX 게이트(예정)
- [x] `S29-1` D1~D5 scorecard 템플릿 검증: 요약 우선/세부 공개 단계/위험 신호 타이밍/결정 CTA 확신 문구/접근성 동등성 기준이 문서화됐는지 확인 (`58-decision-confidence-and-progressive-disclosure-gate-spec.md`)
- [x] `S29-2` progressive disclosure 증적 팩 규격 검증: 요약->세부 전환 캡처와 결정 동선/위험 안내 로그 필수 산출물 정의 여부 확인 (`58` 7장)
- [x] `S29-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S29` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S29-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S29` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 33. 외부 리다이렉트 연속성/복귀 컨텍스트 게이트(예정)
- [x] `S30-1` X1~X5 scorecard 템플릿 검증: 이동 의도/조건 요약/안내 반복 억제/fallback 복구/복귀 상태 유지/접근성·보안·성능 기준이 문서화됐는지 확인 (`59-external-redirect-continuity-and-return-context-gate-spec.md`)
- [x] `S30-2` 증적 팩 규격 검증: 이동 전 안내 캡처 + 인텐트 실패 fallback 로그 + 복귀 컨텍스트 유지 로그 필수 산출물 정의 여부 확인 (`59` 7장)
- [x] `S30-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S30` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S30-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S30` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 34. 지각 지연/로딩 연속성 게이트(예정)
- [x] `S31-1` P1~P5 scorecard 템플릿 검증: 즉시 반응/로딩 연속성/진행-타임아웃 명확성/cache 복원/접근성·저사양 동등성 기준이 문서화됐는지 확인 (`60-perceived-latency-and-loading-continuity-gate-spec.md`)
- [x] `S31-2` 증적 팩 규격 검증: 로딩 전/중/후 캡처 + timeout/retry/fallback 로그 + stale->fresh 전환 로그 필수 산출물 정의 여부 확인 (`60` 7장)
- [x] `S31-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S31` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S31-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S31` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 35. 세션 복귀/중단 복원 게이트(예정)
- [x] `S32-1` R1~R5 scorecard 템플릿 검증: 복귀 위치/입력 보존/재개 동선/복원 실패 복구/접근성·성능 동등성 기준이 문서화됐는지 확인 (`61-session-resume-and-interruption-recovery-gate-spec.md`)
- [x] `S32-2` 증적 팩 규격 검증: 복귀 전/후 캡처 + 백그라운드/kill 복원 로그 + 복원 실패 fallback 로그 필수 산출물 정의 여부 확인 (`61` 7장)
- [x] `S32-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S32` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S32-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S32` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 36. 멀티디바이스 상태 동기화/충돌 복구 게이트(예정)
- [x] `S33-1` M1~M5 scorecard 템플릿 검증: 상태 일관성/충돌 감지·해결/오프라인 복원/재시도 복구/접근성·성능 동등성 기준이 문서화됐는지 확인 (`62-multidevice-state-sync-and-conflict-recovery-gate-spec.md`)
- [x] `S33-2` 증적 팩 규격 검증: 폰/워치/위젯 상태 비교 캡처 + 충돌 해결 로그 + 오프라인 재접속 복원 로그 필수 산출물 정의 여부 확인 (`62` 7장)
- [x] `S33-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S33` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S33-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S33` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 37. 알림 피로도/빈도 개인화 게이트(예정)
- [x] `S34-1` N1~N5 scorecard 템플릿 검증: 과다 억제/개인화 적합성/조용한 시간/제어·복구/접근성·성능·배터리 동등성 기준이 문서화됐는지 확인 (`63-notification-fatigue-and-cadence-personalization-gate-spec.md`)
- [x] `S34-2` 증적 팩 규격 검증: 개인화 전후 빈도 비교 로그 + 조용한 시간 억제 로그 + 설정 반영/복구 로그 필수 산출물 정의 여부 확인 (`63` 7장)
- [x] `S34-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S34` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S34-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S34` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 38. 권한 프릭션/점진적 동의 게이트(예정)
- [x] `S35-1` C1~C5 scorecard 템플릿 검증: 맥락 요청/점진 동의/거부 복구/신뢰 마이크로카피/접근성·성능·배터리 동등성 기준이 문서화됐는지 확인 (`64-permission-friction-and-progressive-consent-gate-spec.md`)
- [x] `S35-2` 증적 팩 규격 검증: 권한 요청 전후 캡처 + 거부/재요청/설정 이동 복구 로그 + 대체 경로 로그 필수 산출물 정의 여부 확인 (`64` 7장)
- [x] `S35-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S35` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S35-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S35` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 39. 실험 플래그/점진 롤아웃 안전 게이트(예정)
- [x] `S36-1` F1~F5 scorecard 템플릿 검증: 분기 일관성/계측 안전성/rollout·rollback 제어/실패 격리/접근성·성능 동등성 기준이 문서화됐는지 확인 (`65-feature-flag-and-staged-rollout-safety-gate-spec.md`)
- [x] `S36-2` 증적 팩 규격 검증: 실험군·대조군 비교 캡처 + rollout/rollback 로그 + 분기 KPI 스냅샷 필수 산출물 정의 여부 확인 (`65` 7장)
- [x] `S36-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S36` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S36-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S36` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 40. 첫 주 온보딩/활성화 연속성 게이트(예정)
- [x] `S37-1` O1~O5 scorecard 템플릿 검증: 첫 가치 도달/온보딩 연속성/초기 개인화/재방문 재개/접근성·성능 동등성 기준이 문서화됐는지 확인 (`66-first-week-onboarding-and-activation-continuity-gate-spec.md`)
- [x] `S37-2` 증적 팩 규격 검증: D0~D7 핵심 동선 캡처 + 첫 성공 도달 로그 + 재방문 재개 로그 필수 산출물 정의 여부 확인 (`66` 7장)
- [x] `S37-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S37` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S37-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S37` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 41. 검색/필터 발견성 및 질의 복원 게이트(예정)
- [x] `S38-1` Q1~Q5 scorecard 템플릿 검증: 진입 발견성/결과 적합성/질의 복원/제어 가능성/접근성·성능 동등성 기준이 문서화됐는지 확인 (`67-search-filter-discoverability-and-query-recovery-gate-spec.md`)
- [x] `S38-2` 증적 팩 규격 검증: 검색·필터 전후 캡처 + 빈 결과 대체 제안 로그 + 질의 복원 로그 필수 산출물 정의 여부 확인 (`67` 7장)
- [x] `S38-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S38` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S38-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S38` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)

## 42. 번호 기록 입력 정확성/중복 방지 게이트(예정)
- [x] `S39-1` T1~T5 scorecard 템플릿 검증: 입력 규칙 명확성/오입력 차단/중복 감지·병합/저장 복원/접근성·성능 동등성 기준이 문서화됐는지 확인 (`68-ticket-entry-accuracy-and-duplicate-prevention-gate-spec.md`)
- [x] `S39-2` 증적 팩 규격 검증: 입력/검증/저장 전후 캡처 + 중복 감지·병합 로그 + 저장 실패 후 복원/재시도 로그 필수 산출물 정의 여부 확인 (`68` 7장)
- [x] `S39-3` 릴리즈 연동 검증: 테스트/우선순위/KPI 문서가 동일한 `S39` ID로 연결됐는지 확인 (`06`, `22`, `23`)
- [x] `S39-4` 루틴 연동 검증: TODO/진척/사이클 리포트/운영 인덱스에 `S39` 루틴 경로가 고정됐는지 확인 (`10`, `11`, `25`, `README`)
