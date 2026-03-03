# 개발 문서 운영 인덱스

## 일정 기준 문서 사용 맵
| 기간 (2026) | 주차 | 핵심 목표 | 반드시 사용하는 문서 |
|---|---|---|---|
| 02-26 ~ 03-04 | W1 | 환경/기반/아키텍처 고정 | 00, 01, 02, 09, 10 |
| 03-05 ~ 03-11 | W2 | 번호생성/저장 안정화 | 03, 04, 05, 09, 10 |
| 03-12 ~ 03-18 | W3 | QR 등록(카메라 스캔) 완성 | 03, 04, 05, 06, 09, 10 |
| 03-19 ~ 03-25 | W4 | 당첨 API/채점 정확도 확보 | 04, 05, 06, 09, 10 |
| 03-26 ~ 04-01 | W5 | 알림/설정 플로우 완성 | 03, 05, 06, 09, 10 |
| 04-02 ~ 04-08 | W6 | 위젯 A/B 동작 완성 | 03, 05, 06, 08, 09, 10 |
| 04-09 ~ 04-15 | W7 | 통계/회귀 테스트 강화 | 05, 06, 09, 10, 11 |
| 04-16 ~ 04-22 | W8 | 릴리즈 준비/인수인계 | 06, 07, 09, 10, 11 |
| 04-23 ~ 05-20 | W9~W12 | 고도화/플랫폼 확장 | 09, 10, 16, 20, 21, 22, 23 |
| 04-30 ~ 05-20 | W10~W12 | 모션/상호작용 고도화 | 08, 10, 22, 23, 24 |
| 05-07 ~ 05-20 | W11~W12 | 비주얼/타이포 리프레시 | 08, 10, 22, 24, 26 |
| 05-14 ~ 05-27 | W12~W13 | UI 비주얼 폴리시 2차 | 08, 10, 16, 22, 26, 27 |
| 05-21 ~ 05-27 | W13 | 승인 패키지 경로/증적 정합성 점검 | 10, 16, 25, 27, 28 |
| 05-28 ~ 06-03 | W14 | missing 산출물 해소 실행 | 10, 25, 28, 29 |
| 06-04 ~ 06-10 | W15 | 안정성/성능 하드닝 실행 | 06, 10, 16, 22, 23, 33 |
| 06-04 ~ 06-10 | W15 | 폰트 자산/라이선스/시각 증적 고정 | 10, 11, 28, 30, 31, 32 |
| 06-11 ~ 06-17 | W16 | 예외 매핑/리다이렉트 상세 스펙 적용 | 08, 10, 11, 16, 25, 33, 34 |
| 06-18 ~ 06-24 | W17 | 유스케이스 리허설/핫패스 성능 최적화 운영 | 06, 07, 10, 11, 16, 22, 25, 33, 35 |
| 06-25 ~ 07-01 | W18 | 하드닝 구현 슬라이스/게이트/롤백 기준 적용 | 06, 07, 08, 10, 11, 16, 22, 25, 33, 34, 35, 36 |
| 07-02 ~ 07-08 | W19 | 성능 게이트 캘리브레이션(환경 분리/반복측정) | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 35, 36, 37 |
| 07-09 ~ 07-15 | W20 | 성능 게이트 실행 템플릿/판정 트리 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 35, 36, 37, 38 |
| 07-16 ~ 07-22 | W21 | 성능 게이트 증적 패키지/최종 결론 템플릿 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 35, 36, 37, 38, 39 |
| 07-23 ~ 07-29 | W22 | UI 품질 게이트/상호작용 안정성 표준 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 26, 27, 33, 37, 38, 39, 40 |
| 07-30 ~ 08-05 | W23 | 통합 품질 결론 패키지/충돌 해소 규칙 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41 |
| 08-06 ~ 08-12 | W24 | 통합 결론 드라이런/에스컬레이션/SLA 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42 |
| 08-13 ~ 08-19 | W25 | 통합 결론 이력/추세/주간 리뷰 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43 |
| 08-20 ~ 08-26 | W26 | 통합 결론 위험예산/프리즈/해제 정책 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44 |
| 08-27 ~ 09-02 | W27 | 프리즈 지휘/커뮤니케이션/사후 회고 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45 |
| 09-03 ~ 09-09 | W28 | 프리즈 드릴/준비도 점수 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46 |
| 09-10 ~ 09-16 | W29 | 드릴 보정 액션 폐쇄 루프 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47 |
| 09-17 ~ 09-23 | W30 | 보정 액션 부채/릴리즈 차단 정책 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48 |
| 09-24 ~ 09-30 | W31 | 보정 액션 부채 이상징후/자동 에스컬레이션 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 |
| 10-01 ~ 10-07 | W32 | 에스컬레이션 대응 용량/커버리지 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 25, 33, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50 |
| 10-08 ~ 10-14 | W33 | UI 미감/타이포 완성도 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 26, 51 |
| 10-15 ~ 10-21 | W34 | 상태 기반 UI 내러티브/마이크로카피 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 26, 51, 52 |
| 10-22 ~ 10-28 | W35 | 상태 전환 코레오그래피/피드백 아이덴티티 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53 |
| 10-29 ~ 11-04 | W36 | 적응형 레이아웃/시선 흐름 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53, 54 |
| 11-05 ~ 11-11 | W37 | 시각 자산 일관성/로딩 fallback 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53, 54, 55 |
| 11-12 ~ 11-18 | W38 | 컨텍스트 우선순위/집중 모드 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53, 54, 55, 56 |
| 11-19 ~ 11-25 | W39 | 상호작용 신뢰 신호/실행 확인 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53, 54, 55, 56, 57 |
| 11-26 ~ 12-02 | W40 | 의사결정 신뢰도/점진적 공개 UX 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 51, 52, 53, 54, 55, 56, 57, 58 |
| 12-03 ~ 12-09 | W41 | 외부 리다이렉트 연속성/복귀 컨텍스트 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 34, 57, 58, 59 |
| 12-10 ~ 12-16 | W42 | 지각 지연/로딩 연속성 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 37, 38, 57, 60 |
| 12-17 ~ 12-23 | W43 | 세션 복귀/중단 복원 게이트 운영 | 06, 07, 08, 10, 11, 16, 22, 23, 24, 25, 34, 57, 60, 61 |
| 12-24 ~ 12-30 | W44 | 멀티디바이스 상태 동기화/충돌 복구 게이트 운영 | 06, 07, 08, 10, 11, 16, 20, 22, 23, 24, 25, 57, 61, 62 |

## 문서별 역할
- `00-product-overview.md`: 제품 목적/범위 고정 기준
- `01-environment-setup.md`: 환경 및 실행 기준
- `02-architecture.md`: 구조/의존/확장 기준
- `03-user-flow.md`: 화면 간 E2E 흐름 기준
- `04-data-api-spec.md`: 데이터 모델/외부 연동 계약 기준
- `05-epics-and-tasks.md`: 에픽 단위 상위 백로그
- `06-test-plan.md`: 테스트 시나리오 및 합격 기준
- `07-release-checklist.md`: 릴리즈 직전 체크리스트
- `08-design-mapping.md`: 디자인 반영 정합성 기준
- `09-development-schedule.md`: 일정/마일스톤/완료 기준
- `10-detailed-todo-board.md`: 실제 실행용 상세 TODO 보드
- `11-progress-tracker.md`: 일일 진행/블로커/결정 로그
- `12-store-metadata.md`: 스토어 설명/릴리즈노트 초안
- `13-privacy-and-permissions.md`: 권한/개인정보 고지 초안
- `14-signing-and-distribution.md`: 서명키/릴리즈 빌드 절차
- `15-incident-response-runbook.md`: 장애 대응 루틴/템플릿
- `16-next-sprint-backlog.md`: 다음 스프린트 우선순위 백로그
- `17-release-preflight-report.md`: 자동 프리플라이트 점검 리포트
- `18-device-validation-report.md`: 실기기 계측 테스트 실행 이력
- `19-offline-design-qa-checklist.md`: Figma 제한 시 오프라인 디자인 정합성 점검 가이드
- `20-wearos-integration-plan.md`: Wear OS(갤럭시 워치) 연동 전략/아키텍처/단계별 실행안
- `21-product-enhancement-ideas.md`: 앱 전체 고도화 아이디어 풀
- `22-prioritization-matrix.md`: 고도화 우선순위 점수/실행 권고
- `23-kpi-and-experiment-plan.md`: KPI 목표/실험 설계/성공 기준
- `24-motion-and-interaction-playbook.md`: 스플래시/내부 모션 규격/QA 기준
- `25-routine-cycle-report.md`: 사이클 단위 코드/품질/UX 진단 리포트
- `26-visual-typography-refresh.md`: 타이포/컬러/컴포넌트 비주얼 리프레시 기준
- `27-ui-visual-polish-pack.md`: 화면별 비주얼 폴리시 2차 실행 가이드
- `28-approval-package-inventory.md`: AJ-005 승인 패키지 산출물 경로/상태 인벤토리
- `29-missing-artifacts-recovery-plan.md`: 승인 패키지 missing 3종 해소 계획
- `33-reliability-and-performance-hardening-plan.md`: 유스케이스 기반 예외처리/안정성/성능 최적화 계획
- `34-exception-mapping-and-redirect-spec.md`: 오류 매핑 표준 + 구매 리다이렉트 fallback 상세 스펙
- `35-usecase-reliability-and-performance-playbook.md`: 사용자 여정 리허설(S05) + 핫패스 성능 운영(S06) 플레이북
- `36-hardening-implementation-slices.md`: 하드닝 구현 슬라이스(S07-1~5) + 게이트/롤백 판정 기준
- `37-performance-gate-calibration-spec.md`: 성능 게이트 캘리브레이션(S08) + profile/반복측정 판정 기준
- `38-performance-gate-execution-template.md`: 성능 게이트 실행 템플릿(S09) + 리포트/판정 트리
- `39-performance-gate-evidence-package-spec.md`: 성능 게이트 증적 패키지(S10) + 최종 결론 제출 템플릿
- `40-ui-quality-gate-and-interaction-resilience-spec.md`: UI 품질 게이트(S11) + 상호작용 안정성/접근성/성능 연동 판정 기준
- `41-unified-quality-verdict-package-spec.md`: 통합 품질 결론 패키지(S12) + `S10/S11` 충돌 해소/단일 결론 규칙
- `42-unified-verdict-dryrun-and-escalation-spec.md`: 통합 결론 드라이런/에스컬레이션(S13) + SLA/액션 게이트 운영 기준
- `43-unified-verdict-history-and-trend-spec.md`: 통합 결론 이력/추세(S14) + 주간 리뷰/클러스터 승격 운영 기준
- `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`: 통합 결론 위험예산/프리즈(S15) + 해제/예외 승인 운영 기준
- `45-freeze-command-and-communication-playbook.md`: 프리즈 지휘/커뮤니케이션(S16) + 해제 회의/사후 회고 운영 기준
- `46-freeze-drill-readiness-score-spec.md`: 프리즈 드릴/준비도 점수(S17) + 시나리오/임계치/점수카드 운영 기준
- `47-freeze-drill-corrective-action-loop-spec.md`: 드릴 보정 액션 폐쇄 루프(S18) + 등록/폐쇄/재개방/SLA 운영 기준
- `48-corrective-action-debt-and-release-block-spec.md`: 보정 액션 부채/릴리즈 차단(S19) + debt 점수/차단·해제/예외 승인 운영 기준
- `49-corrective-action-debt-anomaly-and-escalation-spec.md`: 보정 액션 부채 이상징후/자동 에스컬레이션(S20) + 탐지/경보/SLA/사후 동기화 운영 기준
- `50-escalation-capacity-and-coverage-spec.md`: 에스컬레이션 대응 용량/커버리지(S21) + 커버리지/포화/핸드오버/회복 운영 기준
- `51-visual-aesthetic-and-typography-hardening-spec.md`: UI 미감/타이포 완성도 게이트(S22) + A1~A5 scorecard/증적 팩/릴리즈 연동 기준
- `52-stateful-ui-narrative-and-microcopy-hardening-spec.md`: 상태 기반 UI 내러티브/마이크로카피 게이트(S23) + N1~N5 scorecard/상태 매트릭스/릴리즈 연동 기준
- `53-state-transition-choreography-and-feedback-identity-spec.md`: 상태 전환 코레오그래피/피드백 아이덴티티 게이트(S24) + C1~C5 scorecard/전환 증적 팩/릴리즈 연동 기준
- `54-adaptive-layout-and-attention-flow-gate-spec.md`: 적응형 레이아웃/시선 흐름 게이트(S25) + L1~L5 scorecard/화면 크기별 증적 팩/릴리즈 연동 기준
- `55-visual-asset-consistency-and-fallback-gate-spec.md`: 시각 자산 일관성/로딩 fallback 게이트(S26) + V1~V5 scorecard/자산 증적 팩/릴리즈 연동 기준
- `56-context-priority-and-focus-mode-gate-spec.md`: 컨텍스트 우선순위/집중 모드 게이트(S27) + F1~F5 scorecard/집중 모드 증적 팩/릴리즈 연동 기준
- `57-interaction-trust-signal-and-confirmed-state-gate-spec.md`: 상호작용 신뢰 신호/실행 확인 게이트(S28) + R1~R5 scorecard/confirmed state 증적 팩/릴리즈 연동 기준
- `58-decision-confidence-and-progressive-disclosure-gate-spec.md`: 의사결정 신뢰도/점진적 공개 UX 게이트(S29) + D1~D5 scorecard/progressive disclosure 증적 팩/릴리즈 연동 기준
- `59-external-redirect-continuity-and-return-context-gate-spec.md`: 외부 리다이렉트 연속성/복귀 컨텍스트 게이트(S30) + X1~X5 scorecard/redirect-return continuity 증적 팩/릴리즈 연동 기준
- `60-perceived-latency-and-loading-continuity-gate-spec.md`: 지각 지연/로딩 연속성 게이트(S31) + P1~P5 scorecard/loading continuity 증적 팩/릴리즈 연동 기준
- `61-session-resume-and-interruption-recovery-gate-spec.md`: 세션 복귀/중단 복원 게이트(S32) + R1~R5 scorecard/session recovery 증적 팩/릴리즈 연동 기준
- `62-multidevice-state-sync-and-conflict-recovery-gate-spec.md`: 멀티디바이스 상태 동기화/충돌 복구 게이트(S33) + M1~M5 scorecard/multidevice sync 증적 팩/릴리즈 연동 기준
- `69-physical-blocked-evidence-weekly-routine.md`: 실기기 부재 상태에서 `P-004`/`BK` blocked 증적 주간 갱신 루틴/캘린더 기준
- `70-physical-gates-weekly-summary-template.md`: 물리 게이트 결과 주간 비교 요약 템플릿(출력 규칙/전주 대비 표/운영 체크리스트)
- `71-blocked-state-longtail-risk-criteria.md`: `blocked` 장기화 리스크 기준(임계 주차/L1~L3/에스컬레이션/담당/SLA)
- `72-physical-gates-immediate-execution-bundle.md`: 실기기 연결 즉시 실행 번들 명령(`BK-001`→`BK-002`→`P-004`)과 checkpoint 기반 완료 판정 기준
- `73-physical-blocker-state-sync-checklist.md`: `10`/`11`/`18` 문서 간 블로커 상태 동기화 체크리스트(상태/날짜/증적 경로 일치 규칙)
- `74-physical-device-day0-transition-runbook.md`: 실기기 확보 당일 전환 런북(오케스트레이터 실행/sync/apply/문서 반영 순서와 성공·실패 분기)
- `75-physical-transition-ops-raci-timebox-checklist.md`: 실기기 전환 당일 운영 체크리스트(RACI + 타임박스, 단계별 제한시간/종료 체크)
- `76-p4-evidence-screenshot-log-review-checklist.md`: `P-004` 스크린샷/로그 증적 검수 체크리스트(파일 무결성/capture_fail/로그 품질/판정 규칙)
- `77-firebase-distribution-after-physical-pass-runbook.md`: 실기기 PASS 이후 Firebase Distribution 실행/판정 연계 런북(선행조건/명령 순서/성공·실패 기준/기록 포맷)
- `78-firebase-physical-pass-chain-report-template-guide.md`: `firebase_physical_pass_chain_<date>.md` 템플릿/필드 해석/운영 판정 가이드
- `79-physical-pass-firebase-chain-manual-checklist.md`: BO-001 수동 실행 체크리스트(실행 전/후, 실패 시 즉시 조치, 기록 항목)
- `80-bo005-physical-ready-runner-guide.md`: BO-005 재시도 대기 러너(`run-bo005-when-physical-ready.sh`) 실행/판정 가이드
- `30-font-onboarding-gate.md`: AB-005/006 착수 게이트(체크리스트/승인 기준)
- `31-font-assets-and-license-register.md`: 폰트 자산 반입/라이선스/적용 매핑 레지스터
- `32-visual-proof-matrix-report.md`: AB-009/010 시각 증적 매트릭스/QA 판정 리포트

## 운영 규칙
1. 작업 시작 전 `09`, `10`, `11`을 먼저 확인한다.
2. 기능 머지 전 `06` 테스트 항목을 최소 1회 이상 수행한다.
3. 주차 종료 시 `11`에 완료/미완료/리스크를 반드시 기록한다.
4. 물리 게이트 루틴은 `.github/workflows/physical-gates-routine.yml`(매주 월요일 11:00 KST, `0 2 * * 1`) 또는 `run-physical-gates-routine-check.sh`로 주 1회 이상 점검한다.
5. `blocked`가 2주 이상 연속되면 `71` 기준으로 레벨(L2+)을 판정하고 에스컬레이션 액션을 등록한다.
6. UI 미감/타이포 관련 변경은 `51`의 `S22 A1~A5` scorecard와 전/후 증적 팩을 반드시 첨부한다.
7. 상태 화면(loading/empty/error/offline/redirect) 변경은 `52`의 `S23 N1~N5` scorecard와 상태 매트릭스 증적을 반드시 첨부한다.
8. 상태 전환/피드백 관련 변경은 `53`의 `S24 C1~C5` scorecard와 enter/steady/exit 전환 증적을 반드시 첨부한다.
9. 레이아웃/정보 배치 변경은 `54`의 `S25 L1~L5` scorecard와 소형/표준/대형 증적을 반드시 첨부한다.
10. 시각 자산(아이콘/이미지/배지) 변경은 `55`의 `S26 V1~V5` scorecard와 로딩/실패 fallback 증적을 반드시 첨부한다.
11. 우선순위/집중 모드 변경은 `56`의 `S27 F1~F5` scorecard와 기본/집중 모드 비교 증적을 반드시 첨부한다.
12. 상호작용 피드백/확정 상태 변경은 `57`의 `S28 R1~R5` scorecard와 confirmed state 증적을 반드시 첨부한다.
13. 요약/세부 공개 흐름 변경은 `58`의 `S29 D1~D5` scorecard와 progressive disclosure 증적을 반드시 첨부한다.
14. 외부 이동/복귀 흐름 변경은 `59`의 `S30 X1~X5` scorecard와 redirect-return continuity 증적을 반드시 첨부한다.
15. 로딩/대기/복구 흐름 변경은 `60`의 `S31 P1~P5` scorecard와 loading continuity 증적을 반드시 첨부한다.
16. 앱 복귀/중단 복원 흐름 변경은 `61`의 `S32 R1~R5` scorecard와 session recovery 증적을 반드시 첨부한다.
17. 폰-워치-위젯 상태 동기화 흐름 변경은 `62`의 `S33 M1~M5` scorecard와 multidevice sync 증적을 반드시 첨부한다.

## Cycle-92 Addendum (2026-02-27)
- 일정 맵 확장
  - `W45 (12-31 ~ 01-06)`: 알림 피로도/빈도 개인화 게이트 운영 (`06`, `07`, `08`, `10`, `11`, `16`, `20`, `22`, `23`, `24`, `25`, `57`, `62`, `63`)
- 문서 역할 확장
  - `63-notification-fatigue-and-cadence-personalization-gate-spec.md`: 알림 피로도/빈도 개인화 게이트(S34) + N1~N5 scorecard/notification cadence 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `17.` 알림 빈도/정책 변경은 `63`의 `S34 N1~N5` scorecard와 notification cadence 증적을 반드시 첨부한다.

## Cycle-93 Addendum (2026-02-27)
- 일정 맵 확장
  - `W46 (01-07 ~ 01-13)`: 권한 프릭션/점진적 동의 게이트 운영 (`06`, `07`, `08`, `10`, `11`, `16`, `22`, `23`, `24`, `25`, `34`, `63`, `64`)
- 문서 역할 확장
  - `64-permission-friction-and-progressive-consent-gate-spec.md`: 권한 프릭션/점진적 동의 게이트(S35) + C1~C5 scorecard/permission consent 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `18.` 권한 요청/재요청 흐름 변경은 `64`의 `S35 C1~C5` scorecard와 permission consent 증적을 반드시 첨부한다.

## Cycle-94 Addendum (2026-02-27)
- 일정 맵 확장
  - `W47 (01-14 ~ 01-20)`: 실험 플래그/점진 롤아웃 안전 게이트 운영 (`06`, `07`, `08`, `10`, `11`, `16`, `22`, `23`, `24`, `25`, `41`, `44`, `65`)
- 문서 역할 확장
  - `65-feature-flag-and-staged-rollout-safety-gate-spec.md`: 실험 플래그/점진 롤아웃 안전 게이트(S36) + F1~F5 scorecard/rollout safety 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `19.` 실험군 분기/롤아웃 변경은 `65`의 `S36 F1~F5` scorecard와 rollout safety 증적을 반드시 첨부한다.

## Cycle-95 Addendum (2026-02-27)
- 일정 맵 확장
  - `W48 (01-21 ~ 01-27)`: 첫 주 온보딩/활성화 연속성 게이트 운영 (`03`, `06`, `07`, `08`, `10`, `11`, `16`, `22`, `23`, `24`, `25`, `61`, `66`)
- 문서 역할 확장
  - `66-first-week-onboarding-and-activation-continuity-gate-spec.md`: 첫 주 온보딩/활성화 연속성 게이트(S37) + O1~O5 scorecard/first-week activation 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `20.` 온보딩/초기 활성화 흐름 변경은 `66`의 `S37 O1~O5` scorecard와 first-week activation 증적을 반드시 첨부한다.

## Cycle-96 Addendum (2026-02-27)
- 일정 맵 확장
  - `W49 (01-28 ~ 02-03)`: 검색/필터 발견성 및 질의 복원 게이트 운영 (`03`, `06`, `07`, `08`, `10`, `11`, `16`, `22`, `23`, `24`, `25`, `61`, `67`)
- 문서 역할 확장
  - `67-search-filter-discoverability-and-query-recovery-gate-spec.md`: 검색/필터 발견성 및 질의 복원 게이트(S38) + Q1~Q5 scorecard/query recovery 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `21.` 검색/필터 UX 변경은 `67`의 `S38 Q1~Q5` scorecard와 query recovery 증적을 반드시 첨부한다.

## Cycle-97 Addendum (2026-02-27)
- 일정 맵 확장
  - `W50 (02-04 ~ 02-10)`: 번호 기록 입력 정확성/중복 방지 게이트 운영 (`03`, `06`, `07`, `08`, `10`, `11`, `16`, `22`, `23`, `24`, `25`, `34`, `68`)
- 문서 역할 확장
  - `68-ticket-entry-accuracy-and-duplicate-prevention-gate-spec.md`: 번호 기록 입력 정확성/중복 방지 게이트(S39) + T1~T5 scorecard/duplicate prevention 증적 팩/릴리즈 연동 기준
- 운영 규칙 확장
  - `22.` 번호 저장/수정 흐름 변경은 `68`의 `S39 T1~T5` scorecard와 duplicate prevention 증적을 반드시 첨부한다.
