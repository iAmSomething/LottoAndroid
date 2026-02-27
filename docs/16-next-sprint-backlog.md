# 다음 스프린트 백로그

## 1. 목표
- MVP 이후 고도화 항목을 우선순위 기반으로 실행한다.
- 기능 출시보다 KPI 개선(리텐션/루틴 완료율/안정성)을 우선한다.

## 2. 우선순위 High
- 비주얼/타이포 리프레시 2차 적용(Generator/Manage 확장 + 상호작용 enum 정렬) (`T01`, `R-009`~`R-012`)
- UI 비주얼 폴리시 2차(실폰트/카드 레이어/아이콘 규격화) (`T02`, `AB-001`~`AB-010`) - 핵심 트랙 완료(2026-02-26, `AB-005`~`AB-010` 완료)
- 착수 게이트 고정: `AH-005`(폰트 체크리스트) → `AF-005`(배포 운영 증적) → `AB-005/006`(실폰트 적용) - 게이트 통과 완료(2026-02-26)
- 실폰트 자산 적용 트랙 실행(`AB-005`, `AB-006`) 후 카드/아이콘 폴리시 확장(`AB-007`, `AB-008`) - 완료(2026-02-26)
- 실폰트 자산 패키지/라이선스 체크리스트 선행 확정(`AG-005`, `AH-005`) - 완료(2026-02-26, `31`, `30`)
- Wear OS v1 실제 화면 구현(placeholder → Home/Numbers/Result/Settings 4화면) (`D01`, `K-001`~`K-012`) - 완료(2026-02-26, `wear/src/main/java/com/weeklylotto/wear/WearApp.kt`)
- 워치→폰 핸드오프 최소 경로 구현(`D02`, `K-009`~`K-012`) - 완료(2026-02-26, `wear-remote-interactions` + 딥링크 핸드오프)
- 핵심 상호작용 모션 2차 코드 적용(`G03`, `INT-01`~`INT-05`) - 2차 확대 완료(2026-02-26, `motionClickable` + Home/Result CTA/시트 + Manage/Home 리스트 `animateItem`)
- Reduce Motion 실제 설정/동작 반영(`G06`, `M-012`) - 완료(2026-02-26, Settings 토글 + DataStore + Splash/컴포넌트 반영)
- EXP-05/06 계측 이벤트 훅 + 샘플 검증 자동화 완료(`M-016`~`M-018`) - 완료(2026-02-26, `AnalyticsActionValue` + `scripts/verify-analytics-events.sh`)
- 기획/구현 상태 분리 리포팅 루틴 고정(`P-001`)

## 3. 우선순위 Medium
- 통계 인사이트 확장(출처별 성과/ROI 트렌드/중복도 경고) (`C03`, `C04`, `C01`) - `C03`, `C04`, `C01` 1차 완료(2026-02-26, 출처별 성과 + ROI 트렌드 + 조합 중복도 경고 카드/집계/테스트) / CTA 연계+계측 완료(2026-02-26, `Stats -> Generator` + `interaction_cta_press`) / 로그 샘플 루틴 추가(2026-02-26, `run-analytics-sample-check.sh`, `verify-analytics-events.sh --profile stats-cta`)
- API/로컬 데이터 관측성 스펙 고도화 (`E01`, `L-010`, `L-011`) - 1차 코드/샘플 검증 완료(2026-02-26, `ops_api_request`/`ops_storage_mutation` + `ops-core` 로그 검증), 운영 임계치 자동 판정은 후속
- 단위 테스트 게이트 플래키 감시(`Q01`) - v1 완료(2026-02-27, `run-unit-flaky-guard.sh` + `unit-flaky-guard.yml` 주기 실행 + 로컬 리포트 `unit_flaky_guard_local_2026-02-27.md`)
- 릴리즈 위험 점수 모델 운영 반영(`L-012`) - v1 완료(2026-02-27, `calculate-release-risk-score.sh` + `release-risk-score.yml`)
- 번호관리 빠른 액션 후속 최적화(`A02`) - 완료(2026-02-26, 카드 하단 `상세/이번주 복사/보관/삭제` + 단위 테스트 보강)
- 설정/시크릿 파일 버전관리 정책 정리(`AE-005`, 시크릿 JSON 2종 포함) - v1 완료(2026-02-27, `check-secret-file-policy.sh` + `secret-policy-guard.yml`)
- Firebase App Distribution 운영 검증(`AF-005`, dry-run/실배포 증적) - 완료(2026-02-26, `firebase_dry_run_2026-02-26.md`)
- 배포 주기 점검 루틴 자동화(`I-023`) - 완료(2026-02-26, `firebase-distribution-routine.yml` 주간 스케줄 + `firebase-distribution-routine-check.sh` dry-run 체인 + 첫 CI 실행 run `22436650122` 증적 확보)
- AB-005 착수 승인 패키지 정리(`AJ-005`, 체크리스트/배포 증적/타이포 시안 2종) - 완료(2026-02-26)
- AB-009/AB-010 시각 증적 매트릭스 확정(`AK-005`, 화면 4종×폰트스케일×저조도) - 완료(2026-02-26, `32-visual-proof-matrix-report.md`)
- AB-009/AB-010 증적 운영안 확정(`AL-005`, 파일명 규칙/담당/판정 템플릿)
- AJ-005 산출물 인벤토리 확정(`AM-005`, 실제 파일 경로 매핑/누락 식별)
- AJ-005 누락 3종 해소 계획 확정(`AN-005`, 인벤토리 `missing` → `ready` 전환 계획)
- 누락 항목 1차 증적 확보(`AO-005`, `missing` 3종 중 최소 1개 `ready` 전환) - 완료(2026-02-26, 현재 3종 모두 `ready`)

## 4. 우선순위 Low
- Wear 컴플리케이션/고급 알림 액션(`D03`, `D04`) - v1 완료(2026-02-27, 컴플리케이션 data source + 알림 액션 `앱에서 열기/30분 뒤 다시 알림`)
- 프리미엄 후보 기능 검토(`F01`) - v1 완료(2026-02-27, Stats 커스텀 회차 범위 필터 + 회귀 테스트)
- 프리미엄 후보 기능 검토(`F02`)
- 파트너십/위치 연계 후보 검토(`F04`) - v1 완료(2026-02-27, Home `근처 판매점 찾기` CTA + 지도 앱 fallback/링크 복사 + 로컬 테스트 리포트)

## 5. 완료 기준(DoD)
- High 항목 중 최소 2개는 실제 코드 반영 + 테스트 증적 확보
- 타이포 리프레시 적용 전/후 스크린샷 비교 1세트(Home, Result) 확보
- 실제 폰트 리소스 적용 증적(`res/font` + 전/후 캡처) 1세트 확보
- Wear 4화면 중 최소 2화면은 워치 에뮬레이터 스크린샷/로그 증적 확보
- 모션 항목(SPL/INT) 중 최소 1개는 `Animated*` 계열 적용 증적 확보
- `10-detailed-todo-board.md`에서 High 항목 `50% 이상`이 `[~]` 또는 `[x]`
- `22-prioritization-matrix.md` 점수 재평가 1회 완료
- `11-progress-tracker.md`에 실행 로그 기록 완료
