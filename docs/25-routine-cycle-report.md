# 루틴 사이클 리포트

## 2026-02-26 Cycle-01

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - Feature 화면 22개(`home/generator/manage/manualadd/import/qr/result/settings/stats`) 구성
  - 핵심 레이어(`domain/data/worker/widget`)와 테스트 자산 존재
  - 테스트 자산: 단위 21개, 계측 5개
- 빌드 상태
  - `./gradlew :app:assembleDebug` 성공
- 품질 게이트 상태
  - `./gradlew :app:assembleDebug` 성공
  - `./gradlew :app:testDebugUnitTest` 연속 재실행 2회 성공
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug` 성공

### 2) UI/UX 진행도 진단
- 확인된 강점
  - 홈: 미확인 결과 배지 + 주간 리포트 카드 반영
  - 번호생성: 수동 입력 UX(빠른 후보/팔레트/선택 반영) 고도화
  - 결과: 회차 선택 시트 UX 개선 + 당첨금 요약 반영
  - 번호관리: 필터/정렬/직접 회차 입력 등 관리성 강화
- 확인된 갭
  - 스플래시 애니메이션 체계 부재
  - 내부 모션 시스템 부재
  - 코드 상 `AnimatedVisibility/Crossfade/AnimatedContent` 등 핵심 모션 API 사용 흔적이 매우 제한적

### 3) 이번 루틴에서 도출한 개선안
- 즉시 조치(품질)
  - 단위 테스트 플래키 재현성은 지속 관찰
  - 루틴마다 품질 스냅샷(`assembleDebug`, `testDebugUnitTest`) 유지
- 단기 고도화(UX)
  - 스플래시 콜드/웜 분리 모션 도입
  - 버튼/볼/시트/탭/리스트 상호작용 모션 표준화
  - Reduce Motion 접근성 모드 도입
- 측정/실험
  - EXP-05(스플래시), EXP-06(상호작용 피드백) 실행
  - 모션 이벤트(`motion_*`, `interaction_*`) 계측 설계

### 4) 문서 반영 상태
- 모션/상호작용 기준: `24-motion-and-interaction-playbook.md`
- 실행 보드 매핑: `10-detailed-todo-board.md`의 `M-001~M-020`
- 우선순위/실험/백로그 연계:
  - `22-prioritization-matrix.md`
  - `23-kpi-and-experiment-plan.md`
  - `16-next-sprint-backlog.md`

### 5) 다음 루틴 시작점
1. M-001~M-004(스플래시 시나리오/토큰/브리지/스킵 정책) 문서 구체화
2. EXP-05/06 실험 이벤트 스키마 초안 확정 및 계측 포인트 연결
3. 실기기(모바일/워치) 1차 검증 계획 수립

## 2026-02-26 Cycle-02

### 1) 코드 진행 현황 스냅샷
- 구현 자산 규모
  - `app/src/main/java`: 93개 Kotlin 파일
  - `app/src/test/java`: 21개 Kotlin 파일
  - `app/src/androidTest/java`: 5개 Kotlin 파일
  - `wear` 모듈: 10개 파일(현재 `MainActivity` 단일 placeholder 화면 중심)
- 빌드/품질 상태
  - `./gradlew :app:assembleDebug :app:testDebugUnitTest` 성공
  - `./gradlew :wear:assembleDebug` 성공

### 2) 진행도 진단(기획 vs 구현)
- 모바일 앱(Phone) 영역은 핵심 기능/화면 구현이 진행된 상태
- Wear 영역은 문서상 기획 항목은 폭넓게 완료되었으나, 구현은 초기 스캐폴딩 단계
- 모션 영역은 가이드 문서는 정리되었으나 코드 상 핵심 모션 API 사용은 아직 제한적

### 3) 이번 루틴 고도화 제안
- 상태 관리 체계 분리
  - TODO/백로그에서 "기획 완료"와 "구현 완료"를 분리 표기해 실제 진행률 왜곡 방지
- Wear 실행안 구체화
  - 4개 핵심 화면(Home/Numbers/Result/Settings)의 구현 체크리스트를 코드 단위로 분해
  - 워치→폰 핸드오프 및 Data Layer는 UI 골격 이후 단계적으로 반영
- 스플래시/상호작용 실행안 구체화
  - SPL-01/02, INT-01/02/04/05를 우선 화면에 매핑
  - EXP-05/06 이벤트를 화면/컴포넌트 기준으로 계측 포인트 정의

### 4) 문서 반영 상태
- 실행 보드 추가/정렬: `10-detailed-todo-board.md`(P 섹션)
- 스프린트 재정렬: `16-next-sprint-backlog.md`
- 우선순위 보강: `22-prioritization-matrix.md`
- 모션 적용 매핑 보강: `24-motion-and-interaction-playbook.md`
- EXP-05/06 이벤트 훅 누락 포인트 점검 반영: `23-kpi-and-experiment-plan.md` 8장

### 5) 다음 루틴 시작점
1. P-004(실기기 QA 증적) 블로커 해소 여부 점검
2. Wear 구현 착수 범위(4화면 중 1차 화면) 코드 진행률 확인
3. SPL/INT 우선 시나리오의 실제 화면 반영 여부 재검증

## 2026-02-26 Cycle-03

### 1) 코드 진행 현황 스냅샷
- 구현 자산 규모
  - `app/src/main/java`: 95개 Kotlin 파일
  - `app/src/test/java`: 21개 Kotlin 파일
  - `app/src/androidTest/java`: 5개 Kotlin 파일
  - `wear/src/main/java`: 1개 Kotlin 파일(placeholder 단계)
- 빌드/품질 상태
  - `./gradlew :app:assembleDebug :app:testDebugUnitTest` 성공
  - `./gradlew :wear:assembleDebug` 성공

### 2) 진행도 진단(코딩 기준)
- 새 구현 진척
  - `AnalyticsLogger` 인터페이스 및 `LogcatAnalyticsLogger` 도입
  - `interaction_cta_press`, `interaction_ball_lock_toggle`, `interaction_sheet_apply`가 주요 화면(Home/Generator/Manage/Result)에 연결
  - `interaction_*` 공통 파라미터 키(`screen`, `component`, `action`) 1차 적용
  - 스플래시 motion 이벤트 PR 게이트 적용(`scripts/check-splash-motion-gate.sh`, CI 워크플로우)
- 남은 갭
  - `motion_splash_shown`, `motion_splash_skip`는 여전히 미연결
  - Wear UI는 여전히 단일 placeholder 화면

### 3) 이번 루틴에서 도출한 고도화 제안
- 계측 정합성 고도화
  - `interaction_*` 이벤트 파라미터 스키마를 공통화해 대시보드 집계 가능 상태로 정리
- 모션 실행 연계
  - 스플래시 구현 착수 시 `motion_*` 이벤트를 동시에 연결하도록 게이트화
- Wear 실행 우선순위
  - 4화면 중 Home/Result를 1차 구현 대상으로 고정하고 증적(스크린샷/로그) 수집

### 4) 문서 반영 상태
- 이벤트 훅 점검 업데이트: `23-kpi-and-experiment-plan.md` 8장
- Cycle-03 진단/개선안 기록: 본 문서

### 5) 다음 루틴 시작점
1. Splash 화면 구현과 동시에 `motion_splash_shown`, `motion_splash_skip` 실제 연결
2. `interaction_*` 선택 파라미터 enum 고정(시트 타입/lock 상태/action value)
3. Wear Home/Result 1차 구현 착수 여부 확인

## 2026-02-26 Cycle-04

### 1) 코드 진행 현황 스냅샷
- 구현 자산 규모
  - `app/src/main/java`: 95개 Kotlin 파일
  - `app/src/test/java`: 21개 Kotlin 파일
  - `app/src/androidTest/java`: 5개 Kotlin 파일
  - `wear/src/main/java`: 1개 Kotlin 파일
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` 성공
  - `./gradlew :wear:assembleDebug` 성공
  - `./scripts/check-splash-motion-gate.sh` 성공

### 2) UI/UX 진단(타이포 중심)
- 반영 완료
  - `Type.kt`에 `FontFamily`와 `LottoTypeTokens.Numeric*` 도입
  - `Color.kt` 팔레트 1차 전환(딥틸/웜골드/아이보리)
  - Home/Result 숫자 강조형 스타일 적용
  - 공통 컴포넌트 4종(AppBar/TicketCard/BottomBar/BallChip) 타이포/위계 정렬
- 증적
  - 전/후: `docs/assets/typography-refresh/home_before.png`, `home_after.png`, `result_before.png`, `result_after.png`
  - 접근성 1.3x: `home_font_1_3x.png`, `result_font_1_3x.png`

### 3) 이번 루틴에서 도출한 고도화 제안
- 다음 우선순위
  - Splash 구현 시 `motion_splash_shown`, `motion_splash_skip` 실제 연결
  - `interaction_*` 선택 파라미터 enum 고정(시트 타입/lock 상태/action value)
  - Wear Home/Result 1차 구현 및 실기기 증적 확보

### 4) 문서 반영 상태
- 신규 기준 문서: `26-visual-typography-refresh.md`
- 연동 문서: `08`, `10`, `11`, `16`, `21`, `22`, `README`

### 5) 다음 루틴 시작점
1. Splash 화면 구현과 `motion_*` 이벤트 동시 연결
2. Wear Home/Result 1차 화면 코드 착수
3. 실기기(워치) 증적 수집 계획 확정(P-004)

## 2026-02-26 Cycle-05

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `feature/splash/SplashGate.kt` 신규 추가 및 `MainActivity` 진입 흐름 연결
  - `motion_splash_shown`, `motion_splash_skip` 이벤트 연동
- 품질 스냅샷
  - `./scripts/check-splash-motion-gate.sh` 성공
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - 문서에서 미연결이던 `motion_*` 이벤트가 실제 코드에 연결됨
  - 스플래시 PR 게이트가 실제 스플래시 코드 존재 상황에서도 통과/실패를 판단 가능
- 남은 갭
  - Wear 실기기(소형/대형) 증적은 여전히 부재
  - `motion_*`/`interaction_*` 선택 파라미터 값 enum 고정은 추가 정리 필요

### 3) 증적
- 스플래시 캡처:
  - `docs/assets/typography-refresh/splash_cold.png`
  - `docs/assets/typography-refresh/splash_warm.png`

### 4) 다음 루틴 시작점
1. Wear Home/Result 1차 화면 구현 착수
2. 실기기 기반 P-004 증적 수집 계획 확정
3. 이벤트 파라미터 값(enum) 표준화

## 2026-02-26 Cycle-06

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `:wear` placeholder 제거 후 4화면(Home/Numbers/Result/Settings) 구현
  - 워치→폰 핸드오프 최소 경로(QR/결과/설정 딥링크) 구현
  - `wear-remote-interactions` 의존성 추가
- 품질 스냅샷
  - `./gradlew :wear:assembleDebug` 성공
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - High 우선순위 `D01`, `D02`를 코드 기준으로 완료
  - 워치 화면 구조가 문서 IA(Home/Numbers/Result/Settings)와 일치
- 남은 갭
  - `P-004` 실기기(소형/대형) 증적은 여전히 미수집
  - Data Layer 동기화는 후속 단계

### 3) 증적
- 코드: `wear/src/main/java/com/weeklylotto/wear/WearApp.kt`
- 빌드: 상기 Gradle 명령 2종 성공

### 4) 다음 루틴 시작점
1. Wear 실기기 증적 수집(P-004) 또는 실기기 확보 전까지 에뮬레이터 스크린샷/로그 축적
2. 모션 2차 코드 적용(`G03`, `INT-01`~`INT-05`)
3. Reduce Motion 실제 설정/동작 반영(`G06`)

## 2026-02-26 Cycle-07

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - Reduce Motion 설정 저장소/전역 컨텍스트 추가(`MotionPreferenceStore`, `DataStoreMotionPreferenceStore`, `LocalMotionSettings`)
  - 설정 화면에 `모션 축소` 토글 추가 및 즉시 저장 연동
  - 스플래시/하단탭/번호볼에 모션 축소 규칙 및 애니메이션 2차 반영
  - `AnalyticsActionValue` 도입으로 `action` 파라미터 문자열 상수화
- 품질 스냅샷
  - `./gradlew :app:ktlintFormat :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - High 항목 `G06`(Reduce Motion 실제 동작)이 코드 기준으로 완료
  - `G03` 상호작용 모션 2차가 컴포넌트 단위(`LottoBottomBar`, `BallChip`)로 반영됨
  - 이벤트 파라미터 `action`의 값 드리프트 리스크를 상수화로 축소
- 남은 갭
  - `G03`의 INT-01/03/04 전체 범위는 추가 적용 필요
  - `P-004` 실기기(소형/대형) 증적은 여전히 미수집

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/domain/service/MotionPreferenceStore.kt`
  - `app/src/main/java/com/weeklylotto/app/data/local/DataStoreMotionPreferenceStore.kt`
  - `app/src/main/java/com/weeklylotto/app/ui/theme/Motion.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/splash/SplashGate.kt`
  - `app/src/main/java/com/weeklylotto/app/ui/component/LottoBottomBar.kt`
  - `app/src/main/java/com/weeklylotto/app/ui/component/LottoBall.kt`

### 4) 다음 루틴 시작점
1. 모션 2차 잔여 범위(INT-01/03/04) 화면 반영
2. 실험 연계(`EXP-05/06`) 샘플 로그 검증 및 대시보드 점검
3. Wear 실기기 증적(P-004) 수집 계획 유지

## 2026-02-26 Cycle-08

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `motionClickable` 공통 모디파이어 추가(press/release scale 피드백)
  - Home 핵심 CTA 카드/텍스트 및 Result 회차 시트 선택행에 피드백 적용
  - Home/Manage 목록 카드 전이에 `Modifier.animateItem` 적용
  - `TicketCard` 클릭 경로를 모션 공통 모디파이어로 통일
- 품질 스냅샷
  - `./gradlew :app:ktlintFormat :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` 성공
  - `./gradlew :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - `G03`의 잔여 범위였던 INT-01/03/04를 핵심 화면 기준으로 코드 반영
  - Reduce Motion 설정과 상호작용 피드백이 동일한 공통 모디파이어 경로로 정렬
- 남은 갭
  - `P-004` 실기기(소형/대형) 증적은 여전히 미수집
  - 모션 2차의 전 화면 적용은 후속 루틴에서 확대 가능

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/ui/component/MotionClickable.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/home/HomeScreen.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/result/ResultScreen.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/manage/ManageScreen.kt`
  - `app/src/main/java/com/weeklylotto/app/ui/component/TicketCard.kt`

### 4) 다음 루틴 시작점
1. `P-004` 실기기 증적 확보 경로 유지
2. 실험 대시보드에서 `interaction_*` 이벤트 샘플 검증
3. 모션 2차 적용 범위를 QR/ManualAdd/Settings 화면으로 확장

## 2026-02-26 Cycle-09

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - 버튼형 모션 공통 컴포넌트 추가(`MotionButton`, `MotionTextButton`)
  - 모션 확장 적용: `ManualAddScreen`, `QrScanScreen`, `SettingsScreen`
  - EXP-05/06 로그 검증 스크립트 추가(`scripts/verify-analytics-events.sh`)
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공
  - `./scripts/verify-analytics-events.sh --log-file <sample>` 성공

### 2) 진행도 진단
- 개선점
  - INT-01 press feedback 적용 화면이 생성/스캔/설정 플로우까지 확장됨
  - EXP-05/06 이벤트 연결 상태를 수동 점검이 아닌 스크립트로 재검증 가능해짐
- 남은 갭
  - `P-004` 실기기(워치 소형/대형) 증적은 여전히 미수집
  - 모션 적용을 티켓 상세/통계 세부 CTA까지 확대할 여지 존재

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/ui/component/MotionButtons.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/manualadd/ManualAddScreen.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/qr/QrScanScreen.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/settings/SettingsScreen.kt`
  - `scripts/verify-analytics-events.sh`

### 4) 다음 루틴 시작점
1. `P-004` 실기기 증적 확보(가능 시 워치 소형/대형 우선)
2. 모션 공통 컴포넌트 확장 범위 점검(티켓 상세/통계/가져오기)
3. EXP-05/06 로그를 에뮬레이터 실플로우 기준으로 1회 수집/검증

## 2026-02-26 Cycle-10

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `StatsViewModel` 출처별 성과 집계 모델(`sourceStats`) 추가
  - `StatsScreen`에 `출처별 성과 비교` 카드 추가(자동/수동/QR)
  - `StatsViewModelTest` 출처별 집계 회귀 테스트 2건 추가
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - Medium 우선순위 `C03`를 코드 기준으로 1차 완료
  - 기존 통계 화면의 단순 합계 중심 정보에서 출처 비교 중심 인사이트로 확장
- 남은 갭
  - `C04`(기간별 ROI 트렌드 차트), `C01`(중복도 경고)은 미착수
  - `P-004` 실기기 증적 블로커는 유지

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/feature/stats/StatsViewModel.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/stats/StatsScreen.kt`
  - `app/src/test/java/com/weeklylotto/app/StatsViewModelTest.kt`

### 4) 다음 루틴 시작점
1. `C04`(ROI 트렌드) 최소 차트 버전 범위 정의 및 1차 구현
2. `C01`(중복도 경고) 기준 주차(N)와 경고 임계치 확정
3. `P-004`는 실기기 확보 전까지 블로커 유지 + 에뮬레이터 증적 누적

## 2026-02-26 Cycle-11

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `StatsViewModel`에 회차별 ROI 트렌드 모델(`RoiTrendPoint`) 추가
  - 선택 기간 기준 ROI 집계 추가(회차순, 최대 최근 8회)
  - `StatsScreen`에 `회차별 ROI 트렌드` 카드 추가
  - `StatsViewModelTest` ROI 트렌드 회귀 테스트 2건 추가
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - Medium 우선순위 `C04`를 코드 기준으로 1차 완료
  - 통계 화면에서 기간 필터 + 출처 비교 + 회차 ROI 흐름이 연속적으로 연결됨
- 남은 갭
  - `C01`(중복도 경고) 미착수
  - `P-004` 실기기 증적 블로커 유지

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/feature/stats/StatsViewModel.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/stats/StatsScreen.kt`
  - `app/src/test/java/com/weeklylotto/app/StatsViewModelTest.kt`

### 4) 다음 루틴 시작점
1. `C01`(조합 중복도 경고) 계산 기준 확정 및 1차 구현
2. 통계 카드 간 정보 밀도(스크롤 길이/요약 문구) 미세 조정
3. `P-004` 블로커 유지 + 에뮬레이터 증적 누적

## 2026-02-26 Cycle-12

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `ManualAddViewModel`에 다중 게임 상태/액션 추가(`pendingGames`, `repeatCount`, 반복 추가/삭제)
  - `ManualAddScreen`에 다중 게임 등록 UI 추가(추가 목록/반복 카운트/일괄 저장)
  - 저장 로직 확장: 수동 추가에서 최대 5게임(A~E)을 한 번에 저장
  - `ManualAddViewModelTest`에 다중 저장/반복 저장 케이스 추가
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - 기존 "1게임만 저장" 제약 해소
  - 동일 번호 반복 구매 시나리오를 UX에서 직접 지원
- 남은 갭
  - 기존 수동추가 중복 차단 정책은 유지(이번 주 기존 동일 번호 존재 시 저장 차단)
  - `P-004` 실기기 증적 블로커 유지

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/feature/manualadd/ManualAddViewModel.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/manualadd/ManualAddScreen.kt`
  - `app/src/test/java/com/weeklylotto/app/ManualAddViewModelTest.kt`

### 4) 다음 루틴 시작점
1. `C01`(중복도 경고) 1차 구현 착수
2. 수동추가 다중 게임 UX 미세 조정(문구/밀도/버튼 우선순위)
3. `P-004` 블로커 유지 + 에뮬레이터 증적 누적

## 2026-02-26 Cycle-13

### 1) 코드 진행 현황 스냅샷
- 구현 범위
  - `NumberGeneratorViewModel` 수동 반영 로직 확장(`replaceTargetNumber`)
  - 교체 대상 유효성 검증 추가(잠금 번호/존재하지 않는 번호 차단)
  - `NumberGeneratorScreen`에 교체 대상 직접 선택 UI/안내 문구 추가
  - `NumberGeneratorViewModelTest`에 교체 대상 회귀 테스트 2건 추가
- 품질 스냅샷
  - `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` 성공

### 2) 진행도 진단
- 개선점
  - 기존 "선택 반영 시 어떤 번호가 바뀌는지 모호함" 문제를 해소
  - 사용자가 교체 대상을 명시적으로 선택할 수 있어 예측 가능성 향상
- 남은 갭
  - Generator 수동 편집의 단계 수는 여전히 많은 편(후속 단순화 가능)
  - `P-004` 실기기 증적 블로커 유지

### 3) 증적
- 코드:
  - `app/src/main/java/com/weeklylotto/app/feature/generator/NumberGeneratorViewModel.kt`
  - `app/src/main/java/com/weeklylotto/app/feature/generator/NumberGeneratorScreen.kt`
  - `app/src/test/java/com/weeklylotto/app/NumberGeneratorViewModelTest.kt`

### 4) 다음 루틴 시작점
1. `C01`(중복도 경고) 1차 구현 착수
2. Generator 수동 편집 단계 단순화(후보/팔레트/입력 흐름 재정렬) 검토
3. `P-004` 블로커 유지 + 에뮬레이터 증적 누적
