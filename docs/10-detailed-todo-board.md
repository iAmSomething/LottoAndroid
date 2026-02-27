# 상세 TODO 보드

## 사용 규칙
- 상태 표기: `[ ]` 대기, `[~]` 진행중, `[x]` 완료, `[!]` 블로커
- 모든 항목은 완료 시 `11-progress-tracker.md`에 증적(명령/스크린샷/로그)을 남긴다.

## A. 기반/환경
- [x] A-001 루트 Gradle/Wrapper 구성
- [x] A-002 `app` 모듈 생성 및 `applicationId` 설정
- [x] A-003 Compose/Room/WorkManager/Glance 의존성 추가
- [x] A-004 CI 워크플로우(`assembleDebug`, `test`, `ktlint`, `detekt`) 구성
- [x] A-005 기본 테마/리소스/앱 아이콘 설정
- [x] A-006 AGP를 compileSdk 35 정식 지원 버전으로 상향

## B. 번호 생성/관리
- [x] B-001 5게임 자동 생성 로직 구현
- [x] B-002 잠금 토글 로직 구현
- [x] B-003 잠금 제외 재생성 로직 구현
- [x] B-004 수동 입력 반영 로직 구현
- [x] B-005 이번 주 번호 저장 구현
- [x] B-006 번호 편집 UX(키패드/오류 메시지 세분화) 개선
- [x] B-007 저장 시 중복 bundle 정책(덮어쓰기/누적) 고도화
- [x] B-008 판매 회차 기준 통일(다음 추첨 회차 기준으로 홈/번호관리/저장 동기화)
- [x] B-009 번호관리 편집모드 이동 기능 구현(선택 번호 `SAVED` 상태 전환 + 보관함 탭 반영)
- [x] B-010 티켓 상세 `이번주로 복사` 기능 구현(과거 회차 → 이번주 회차 복제 저장)
- [x] B-011 티켓 상세 복사 중복 방지(이번주 동일 번호 존재 시 저장 차단)
- [x] B-012 티켓 상세 요약 카드 추가(회차/상태/추첨일/출처/등록일 정보 구조화)
- [x] B-013 번호관리 보관함 요약 카드 추가(총 건수/보관 건수/당첨 건수)
- [x] B-014 번호관리 스캔내역 요약 카드 추가(총 스캔/이번주 스캔/최신 회차)
- [x] B-015 번호관리 스캔내역 메타 강화(스캔 등록 시각 + 게임 수)
- [x] B-016 수동추가/가져오기 저장 중복 방지(이번주 동일 번호 차단)
- [x] B-017 번호관리 필터 시트 단순화(전체 상태 칩/활성 필터 카운트/적용 요약)
- [x] B-018 번호관리 회차 필터 직접 입력 추가(시작/끝 회차 범위 검증 후 적용)
- [x] B-019 번호관리 목록 정렬 옵션 추가(최신등록순/오래된등록순/회차 높은순)
- [x] B-020 번호생성 수동 입력 UX 개편(빠른 후보/번호 팔레트/선택 반영 플로우)

## C. QR 등록
- [x] C-001 QR URL 파서(2포맷) 구현
- [x] C-002 QR 화면 수동 입력 백업 경로 구현
- [x] C-003 CameraX 프리뷰 + ML Kit 분석기 구현
- [x] C-004 스캔 중복 처리(5초 디바운스) 구현
- [x] C-005 실제 동행복권 QR 샘플 회귀 테스트 케이스 확장
- [x] C-006 카메라 스캔 실패 UX(재시도/가이드) 개선
- [x] C-007 실사진 기준 가이드 문구 반영(우측 상단 QR, 다중 용지 분리 촬영)
- [x] C-008 실사진 기준 거리/각도 가이드 오버레이 UI 추가
- [x] C-009 다중 티켓 연속 스캔 모드(저장 후 자동 재시작) 구현
- [x] C-010 저조도/반사 대응 UX(플래시 토글/환경 가이드/실패 누적 권장 액션) 추가

## D. 당첨 연동/채점
- [x] D-001 당첨 API 클라이언트 구현
- [x] D-002 최신 회차 fetch + 로컬 fallback 구현
- [x] D-003 채점기(1~5등/낙첨/보너스) 구현
- [x] D-004 결과 화면 하이라이트 구현
- [x] D-005 API 예외 분기별 사용자 메시지 표준화
- [x] D-006 회차 추정 로직 검증 케이스 보강
- [x] D-007 공식 API 차단 시 미러 endpoint 자동 fallback 구현/테스트
- [x] D-008 Result 네트워크 실패 UX 개선(자동 재시도/실패시각/최신회차 재조회)
- [x] D-009 Result 회차 변경 시트 UX 보강(라디오 선택/선택행 강조/취소·적용 버튼)
- [x] D-010 Result 당첨금 표시 추가(게임별 예상 당첨금 + 합계 요약)

## E. 알림
- [x] E-001 구매/결과 Worker 구현
- [x] E-002 주간 반복 스케줄러 구현
- [x] E-003 설정 화면 프리셋 토글 구현
- [x] E-004 설정값 DataStore 영속화
- [x] E-005 부팅 후 자동 복구(`BOOT_COMPLETED`) 연동

## F. 위젯
- [x] F-001 타입 A 위젯 골격 구현
- [x] F-002 타입 B 위젯 골격 구현
- [x] F-003 디자인 밀도(번호볼/요약문구) 고도화
- [x] F-004 데이터 변경 이벤트 기반 위젯 강제 갱신 구현
- [x] F-005 위젯 액션(앱 특정 화면 deep link) 강화
- [x] F-006 디버그 전용 위젯 갱신 이력 로깅 및 조회 스크립트 추가
- [x] F-007 위젯 A/B 시각 디자인 전면 개선(카드형 계층/강조 CTA/가독성 보강)

## G. 통계
- [x] G-001 누적 구매금/게임수 계산 구현
- [x] G-002 TOP 번호 계산 구현
- [x] G-003 당첨금 누적 계산 연동
- [x] G-004 기간 필터(최근 4주/전체) 추가
- [x] G-005 기간 필터 확장(최근 8주) 및 회귀 테스트 추가

## H. 테스트/품질
- [x] H-001 번호생성 테스트 추가
- [x] H-002 채점 테스트 추가
- [x] H-003 QR 파싱 테스트 추가
- [x] H-004 `ktlintCheck`, `detekt`, `testDebugUnitTest` 파이프라인 통과
- [x] H-005 UI 테스트(Compose) 작성
- [x] H-006 통합 테스트(Room + Repository) 작성
- [x] H-007 회차 계산 회귀 테스트 추가(`nextDrawDate`, `currentSalesRound`)
- [x] H-008 번호생성 저장 E2E 계측 회귀 테스트 추가(`WeeklySaveFlowInstrumentedTest`)
- [x] H-009 ResultViewModel 재시도 로직 단위 테스트 추가
- [x] H-010 실기기 계측 검증 자동화 스크립트/리포트 템플릿 추가(`scripts/run-physical-device-validation.sh`, `docs/18-device-validation-report.md`)
- [x] H-011 공통 컴포넌트 스크린리더 문구/접근성 역할 보강(`BallChip`, `TicketCard`, `LottoTopAppBar`)
- [x] H-012 테마 색상 콘트라스트 자동 검증 추가(`ColorContrastTest`, WCAG AA 4.5:1 기준)
- [x] H-013 상태/출처/모드 한국어 라벨 매핑 공통화 및 회귀 테스트 추가(`LottoUiLabelsTest`)
- [x] H-014 계측 회귀 안정화: `WeeklySaveFlowInstrumentedTest`의 Espresso back 의존 제거(Compose dispatcher back으로 전환)
- [x] H-015 번호관리 이동 플로우 회귀 테스트 추가(`ManageViewModelTest`, `RoomTicketRepositoryIntegrationTest`)
- [x] H-016 티켓 상세 복사 플로우 회귀 테스트 추가(`ManageViewModelTest` 복사 성공/차단 케이스)
- [x] H-017 티켓 상세 복사 중복 차단 회귀 테스트 추가(`ManageViewModelTest` 동일 번호 중복 방지)
- [x] H-018 Result 회차 선택/재조회 흐름 단위 테스트 추가(`ResultViewModelTest`)
- [x] H-019 번호관리 탭 필터 회귀 테스트 추가(`ManageViewModelTest` 이번주/스캔탭 노출 규칙)
- [x] H-020 번호관리 스캔 요약 계산 테스트 추가(`ManageViewModelTest` scanSummary)
- [x] H-021 수동추가/가져오기 중복 저장 차단 테스트 추가(`ManualAddViewModelTest`, `ImportViewModelTest`)
- [x] H-022 번호관리 필터 정규화 테스트 추가(`ManageViewModelTest` WEEK 전환 시 SAVED 제거, 상태필터만 초기화)
- [x] H-023 번호관리 회차 필터 범위 테스트 추가(`ManageViewModelTest` 직접범위/전체초기화)
- [x] H-024 번호관리 정렬 회귀 테스트 추가(`ManageViewModelTest` 기본/오래된순/회차순)
- [x] H-025 당첨금 정책/결과 합계 회귀 테스트 추가(`PrizeAmountPolicyTest`, `ResultViewModelTest`)

## I. 릴리즈 준비
- [x] I-001 스토어 메타데이터 작성
- [x] I-002 권한/개인정보 안내문 작성
- [x] I-003 서명키/배포 프로세스 점검
- [x] I-004 최종 회귀 체크리스트 전항목 완료
- [x] I-005 릴리즈 프리플라이트 자동 점검 스크립트 추가 및 실행
- [x] I-006 로컬 릴리즈 키 생성/백업 자동화 스크립트 추가 및 검증
- [x] I-007 GitHub Secrets 동기화 스크립트 추가 및 dry-run 검증
- [x] I-008 Secrets 업로드 스크립트 안전모드(`--apply` 명시) 보강
- [x] I-009 Secrets 업로드 대상 Android 저장소 검증 가드 추가 및 오업로드 정리
- [x] I-010 Secrets 업로드 대상 repo 자동탐지(단일 후보) 및 안전중단 로직 추가
- [x] I-011 앱 아이콘 브랜딩 리뉴얼(adaptive foreground/background/monochrome)
- [x] I-012 `release-preflight.sh --with-build` 로컬 품질게이트에 `detekt` 포함
- [x] I-013 프리플라이트 실기기 엄격 모드(`--require-physical-device`) 추가
- [x] I-014 실기기 엄격 모드 fail-fast(미충족 시 품질게이트 생략) 적용
- [x] I-015 프리플라이트 테스트 대상 serial 지정 옵션(`--android-serial`) 추가
- [x] I-016 엄격+serial 조합 검증(에뮬레이터 지정 시 의도된 실패 확인)
- [x] I-017 최종 점검 래퍼(`release-final-check.sh`) 추가: 실기기 우선, 미보유 시 에뮬레이터 fallback 지원
- [x] I-018 에뮬레이터/무기기 환경 fallback 보강(`release-final-check.sh`: CI-only 자동 전환)
- [x] I-019 에뮬레이터 시작 ANR 대응 프리플라이트 보강(`connectedDebugAndroidTest` 1회 자동 재시도)
- [x] I-020 앱 스타트업 ANR 완화: `AppGraph.init` 경량화 + 의존성 lazy 초기화
- [x] I-021 에뮬레이터 설치/연결 불안정 대응: 프리플라이트 재시도 3회로 확장(Broken pipe/offline/startup 감지)
- [x] I-022 런처 아이콘 콘셉트 교체: 딥틸 배경 + 상승하는 행운(Adaptive foreground/monochrome)
- [x] I-023 Firebase 배포 주기 점검 루틴 자동화(`firebase-distribution-routine.yml`, `firebase-distribution-routine-check.sh`, `firebase-distribute.sh --dry-run`)
- [x] I-024 Firebase 배포 주기 점검 첫 CI 실행 증적 수집(run `22436650122`, artifact `firebase_routine_ci_22436650122.md`)

## J. 디자인 스펙 정렬(`/Volumes/무제/design_spec_android_lotto.md`)
- [x] J-001 토큰(색상/간격/타이포) 1차 반영
- [x] J-002 공통 컴포넌트(AppBar/BottomBar/BallChip/TicketCard/Badge) 추가
- [x] J-003 MainNav 3탭(Home/Manage/Result) 구조 반영
- [x] J-004 Manage 서브뷰(편집모드/FAB시트/필터시트/삭제 다이얼로그/상세) 1차 구현
- [x] J-005 신규 라우트(ManualAdd/Import/Qr/TicketDetail) 구현
- [x] J-006 Result 회차 피커와 실제 회차 데이터 스위칭 연동
- [x] J-007 Figma/스펙 기준 간격·타이포 픽셀 보정(컴포넌트/핵심 화면)
- [x] J-008 Ticket 상세 화면 공유 액션 및 공유 텍스트 포맷 구현
- [x] J-009 Figma 호출 제한 대응 오프라인 디자인 QA 체크리스트 문서화
- [x] J-010 QR 카메라 바인딩 안정화(라이프사이클 destroyed 가드, 콜백 최신화)로 계측 크래시 리스크 완화
- [x] J-011 Manage 편집모드 `이동` 액션을 실동작으로 연결(이동 시트 + 보관함 상태 배지)
- [x] J-012 Ticket Detail 상단 공유 액션 + 하단 `이번주로 복사` CTA 정렬
- [x] J-013 Ticket Detail 정보 요약 카드 UI 반영(스펙 6.5 SummaryCard 대응)
- [x] J-014 Result 회차 선택 시트 인터랙션 개선(선택 상태 시각화 + 취소 동선)
- [x] J-015 Manage 보관함 상단 요약 카드 UI 반영(스펙 5.4.2 폴더 카드 대응)
- [x] J-016 Manage 스캔내역 상단 요약 카드 UI 반영(스펙 5.4.3 정보 밀도 보강)
- [x] J-017 Manage 필터 시트 UX 개선(탭별 상태칩/전체칩/적용중 요약 문구)
- [x] J-018 Manage 필터 시트 회차 직접 입력 UI 반영(시작/끝 입력 + 오류 문구 + 적용 버튼)
- [x] J-019 Manage 정렬 시트 UI 반영(최신순/오래된순/회차순 + 설명 문구)
- [x] J-020 NumberGenerator 수동 입력 인터랙션 재설계(선택 중심 편집 + 빠른 후보)
- [x] J-021 Widget A/B 비주얼 리디자인(브랜드 컬러 카드 + 액션 버튼 가시성 개선)

## K. Wear OS(갤럭시 워치) 연동
- [x] K-001 Wear OS IA 확정(Home/Numbers/Result/Settings 4탭)
- [x] K-002 원형 UI 레이아웃 가이드 확정(터치 타겟/폰트/여백)
- [x] K-003 워치 전용 디자인 토큰 세트 정의(색/크기/타이포)
- [x] K-004 워치 내비게이션 플로우 및 back-stack 규칙 문서화
- [x] K-005 홈 화면(회차/D-day/핵심 CTA) 와이어프레임 확정
- [x] K-006 번호 화면(A~E 요약) 카드 정보 밀도 검증
- [x] K-007 결과 화면(당첨번호/내 결과 요약) 상태 표현 확정
- [x] K-008 설정 화면(알림/진동/핸드오프) UX 문구 확정
- [x] K-009 폰 핸드오프 UX 정의(QR은 폰에서 이어서 수행)
- [x] K-010 Wear Data Layer 계약서 작성(path/payload/versioning)
- [x] K-011 Android ↔ Wear 동기화 충돌 정책 정의(updatedAt/idempotency)
- [x] K-012 오프라인 캐시/재연결 동기화 시나리오 정의
- [x] K-013 워치 알림 정책 정의(구매/결과/요약 빈도)
- [x] K-014 원형 소형/대형 디바이스 QA 매트릭스 작성
- [x] K-015 성능 목표 정의(첫 렌더/스크롤/배터리 영향)
- [x] K-016 접근성 기준 정의(대비/스크린리더/동작 시간)
- [x] K-017 베타 테스트 플랜(Test case + fail 기준) 작성
- [x] K-018 스토어 메타데이터 초안 분리(모바일/워치)
- [x] K-019 권한/개인정보 고지문 Wear 항목 추가
- [x] K-020 릴리즈 게이트 정의(동기화 성공률/치명 이슈 0건)

## L. 앱 전체 고도화(리텐션/인사이트/운영)
- [x] L-001 미확인 결과 배지 UX 기획/문구 확정
- [x] L-002 미확인 결과 배지 실험 설계(EXP-01) 및 이벤트 정의
- [x] L-003 주간 리포트 카드 IA/카피 확정
- [x] L-004 주간 리포트 카드 실험 설계(EXP-02) 및 성공 기준 확정
- [x] L-005 번호관리 빠른 액션 후보 정의(복사/이동/삭제 단축)
- [x] L-006 번호관리 빠른 액션 유저 플로우/Baseline 탭 수 측정
- [x] L-007 통계 출처별 성과 비교 지표 정의(자동/수동/QR)
- [x] L-008 ROI 트렌드 시각화 컴포넌트 요구사항 정의
- [x] L-009 조합 중복도 경고 로직 스펙 정의(최근 N주 기준)
- [x] L-010 API 지연/실패율 모니터링 지표와 임계치 정의
- [x] L-011 로컬 데이터 무결성 점검 체크 규칙 정의
- [x] L-012 릴리즈 위험 점수 산식 초안(변경량/테스트/결함) 작성
- [x] L-013 KPI 대시보드 템플릿 작성(WAU/리텐션/루틴완료율)
- [x] L-014 실험 운영 주기 규칙 정의(2주/4주 판정)
- [x] L-015 실패 실험 종료 기준 및 롤백 규칙 문서화
- [x] L-016 홈 미확인 결과 배지 구현(최신 결과 미확인 라운드 강조 + 결과화면 진입 동선)
- [x] L-017 홈 주간 리포트 카드 구현(구매/당첨/순이익 요약 노출)
- [x] L-018 번호관리 카드 하단 빠른 액션 구현(상세/이번주 복사/보관/삭제)
- [x] L-019 번호관리 빠른 액션 회귀 테스트 추가(`ManageViewModelTest` 단건 보관/삭제 요청)
- [x] L-020 통계 조합 중복도 경고 1차 구현(`C01`: 선택 기간 기준 중복률/최빈 조합/경고 레벨 + `StatsViewModelTest`)
- [x] L-021 통계 중복도 경고 → 번호생성 CTA 연계(`StatsScreen` 버튼 + `Stats -> Generator` 네비게이션)
- [x] L-022 통계 CTA 계측 이벤트 연동(`interaction_cta_press`, `screen=stats`, `component=duplicate_warning_card`)
- [x] L-023 통계 CTA 로그 샘플 수집 루틴 추가(`run-analytics-sample-check.sh` + `verify-analytics-events.sh --profile stats-cta`)
- [x] L-024 API/로컬 저장 관측성 1차 코드 반영(`DrawApiClient`: `ops_api_request`, `RoomTicketRepository`: `ops_storage_mutation`)
- [x] L-025 운영 관측성 로그 샘플 수집 루틴 추가(`run-ops-observability-check.sh` + `verify-analytics-events.sh --profile ops-core`)

## M. 스플래시/상호작용 모션 고도화
- [x] M-001 스플래시 시나리오 정의(콜드/웜/오류 브리지)
- [x] M-002 스플래시 모션 토큰 정의(duration/easing/delay)
- [x] M-003 홈 초기 로딩 skeleton 전환 규칙 정의
- [x] M-004 스플래시 스킵/축약 정책 정의(첫 실행 vs 재실행)
- [x] M-005 버튼 press/release 피드백 표준화
- [x] M-006 BallChip 상태 전이 모션 표준화(선택/잠금/적중/보너스)
- [x] M-007 탭 전환 모션 표준화(crossfade/indicator)
- [x] M-008 시트/다이얼로그 오픈/닫힘 모션 표준화
- [x] M-009 리스트 추가/삭제/정렬 전이 모션 표준화
- [x] M-010 저장/복사/삭제 완료 피드백 패턴 정의(inline/toast/haptic)
- [x] M-011 오류 상태 피드백 패턴 정의(진동/색/모션 강도)
- [x] M-012 Reduce Motion 모드 정책 정의(축소 규칙/예외)
- [x] M-013 모션 접근성 점검 체크리스트 작성
- [x] M-014 모션 성능 목표 정의(FPS, jank 허용치, 배터리)
- [x] M-015 저사양 기기 fallback 정책 정의
- [x] M-016 모션 이벤트 로깅 스키마 정의(`motion_*`, `interaction_*`)
- [x] M-017 EXP-05 실험 설계/대조군 분기 정의
- [x] M-018 EXP-06 실험 설계/성공지표 확정
- [x] M-019 모션 QA 리포트 템플릿 작성(기기/시나리오/결함)
- [x] M-020 릴리즈 게이트 반영(모션 품질 항목)

## N. 홈 인사이트 연동
- [x] N-001 Result 확인 회차 추적 스토어 추가(DataStore 기반 `ResultViewTracker`)
- [x] N-002 Result 진입 시 확인 회차 기록 연동(`ResultViewModel`)
- [x] N-003 Home 인사이트 계산 로직 추가(최신회차 기준 주간 리포트/미확인 배지)
- [x] N-004 Home 인사이트 UI 반영(배지 카드 + 주간 리포트 카드)
- [x] N-005 회귀 테스트 추가(`HomeViewModelTest`, `ResultViewModelTest`)

## O. 루틴 점검 즉시 조치(2026-02-26 Cycle-01)
- [x] O-001 단위 테스트 컴파일 오류 복구 상태 확인(`DefaultWidgetDataProviderTest`, `HomeViewModelTest`)
- [x] O-002 `./gradlew :app:testDebugUnitTest` 연속 Green 확인
- [x] O-003 테스트 더블 네이밍/스코프 충돌 방지 규칙 문서화
- [x] O-004 루틴 품질 스냅샷 표준화(`assembleDebug` + `testDebugUnitTest` 결과 기록)

## P. 기획-구현 간극 정렬(2026-02-26 Cycle-02)
- [x] P-001 상태 관리 규칙 보강: "기획 완료"와 "구현 완료"를 루틴 리포트에서 분리 기록
- [x] P-002 Wear 구현 베이스라인 기록: `:wear`는 현재 placeholder 단계임을 명시
- [x] P-003 모션 적용 우선 맵 문서화(스플래시/CTA/BallChip/시트/탭 중심)
- [!] P-004 실기기 증적 확보: Wear 소형/대형 2종에서 첫 렌더/스크롤/터치 검증 로그 수집 (실기기 미보유로 블로커)
- [x] P-005 EXP-05/06 이벤트 훅 연결 점검: `motion_*`, `interaction_*` 누락 포인트 식별

## Q. 루틴 점검 즉시 조치(2026-02-26 Cycle-03)
- [x] Q-001 `AnalyticsLogger`/`LogcatAnalyticsLogger` 도입 상태 확인 및 DI 연결 점검
- [x] Q-002 `interaction_*` 이벤트 훅 연결 상태 확인(Home/Generator/Manage/Result)
- [x] Q-003 `motion_*` 이벤트 미연결 상태 문서 반영(`23`, `24`, `25`)
- [x] Q-004 `interaction_*` 공통 파라미터 스키마(`screen/component/action`) 정의 및 보드 반영
- [x] Q-005 스플래시 구현 PR 게이트에 `motion_*` 연결 조건 추가(`scripts/check-splash-motion-gate.sh`, CI 워크플로우 연동)

## R. 비주얼/타이포 리프레시(2026-02-26 Cycle-04)
- [x] R-001 타이포 문제 진단 문서화(`fontFamily` 미지정/위계 평면화)
- [x] R-002 리프레시 콘셉트 정의(`Lucky Editorial`)
- [x] R-003 폰트 페어/타입 스케일 가이드 작성(`26-visual-typography-refresh.md`)
- [x] R-004 컬러 톤 리프레시 가이드 작성(딥틸/웜골드/아이보리)
- [x] R-005 디자인 매핑 연동(`08-design-mapping.md` 8장)
- [x] R-006 우선순위/백로그 반영(`22`, `16`, `README`)
- [x] R-007 Home/Result 타이포 1차 코드 반영 및 전/후 스크린샷 수집(`docs/assets/typography-refresh`)
- [x] R-008 `Type.kt` 폰트 리소스 연결(`FontFamily`) 설계 확정(현재 시스템 fallback 기반)
- [x] R-009 숫자 강조 스타일(`Numeric`) 공통 토큰 정의(`LottoTypeTokens`)
- [x] R-010 컴포넌트 4종(AppBar/TicketCard/BottomBar/BallChip) 비주얼 통일 적용
- [x] R-011 접근성 1.3x 폰트 스케일 QA(에뮬레이터 캡처 증적 확보)
- [x] R-012 Wear 타이포 밀도 가이드(원형 UI 전용) 확정(`26` 11장)

## S. 스플래시/모션 코드 구현(2026-02-26 Cycle-05)
- [x] S-001 `SplashGate` 구현(콜드 900ms/웜 300ms 브리지)
- [x] S-002 `motion_splash_shown`, `motion_splash_skip` 이벤트 연결
- [x] S-003 스플래시 캡처 증적 수집(`docs/assets/typography-refresh/splash_cold.png`, `splash_warm.png`)

## T. Wear v1 코드 구현(2026-02-26 Cycle-06)
- [x] T-001 `:wear` 실제 4화면(Home/Numbers/Result/Settings) 내비게이션 구현(`WearApp.kt`)
- [x] T-002 워치→폰 핸드오프 최소 경로 구현(QR/Result/Settings 딥링크 오픈)
- [x] T-003 Wear 원형 UI 번호볼/요약 카드/토글 설정 UI 반영(정보 밀도 기준 적용)
- [x] T-004 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug`)

## U. 모션 2차 + Reduce Motion 코드 반영(2026-02-26 Cycle-07)
- [x] U-001 `MotionPreferenceStore` 도입 및 DataStore 영속화(`DataStoreMotionPreferenceStore`)
- [x] U-002 설정 화면에 `모션 축소` 토글 추가 및 즉시 저장 반영(`SettingsViewModel/SettingsScreen`)
- [x] U-003 앱 전역 모션 컨텍스트 도입(`LocalMotionSettings`) 및 `MainActivity` 연동
- [x] U-004 `SplashGate` Reduce Motion 규칙 반영(시간 50% 축소, scale transform 최소화)
- [x] U-005 상호작용 모션 2차 적용(`LottoBottomBar`, `BallChip` animateColor/scale)
- [x] U-006 `interaction_*`, `motion_*` action 값 상수화(`AnalyticsActionValue`) 및 화면 이벤트 반영
- [x] U-007 품질 스냅샷 재검증(`./gradlew :app:ktlintFormat :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug`)

## V. 모션 2차 잔여 범위 적용(2026-02-26 Cycle-08)
- [x] V-001 CTA press/release 공통 모디파이어 도입(`motionClickable`)
- [x] V-002 Home 핵심 CTA 카드/텍스트 액션에 press feedback 반영
- [x] V-003 Result 회차 시트 선택행/회차 변경 액션에 press feedback 반영
- [x] V-004 Manage/Home 리스트 카드 전이 애니메이션 반영(`Modifier.animateItem`)
- [x] V-005 `TicketCard` 클릭 경로를 모션 공통 모디파이어로 통일
- [x] V-006 품질 스냅샷 재검증(`./gradlew :app:ktlintFormat :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` + `:wear:assembleDebug`)

## W. 모션 확장 + EXP-05/06 검증 자동화(2026-02-26 Cycle-09)
- [x] W-001 버튼형 공통 컴포넌트 도입(`MotionButton`, `MotionTextButton`)
- [x] W-002 수동추가 화면 모션 확장(번호 선택 터치 + CTA 버튼)
- [x] W-003 QR/설정 화면 모션 확장(시트/닫기/저장/프리셋 액션)
- [x] W-004 EXP-05/06 샘플 이벤트 검증 스크립트 추가(`scripts/verify-analytics-events.sh`)
- [x] W-005 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug`)

## X. 통계 인사이트 확장 C03(2026-02-26 Cycle-10)
- [x] X-001 `StatsViewModel`에 출처별 성과 집계 모델 추가(자동/수동/QR)
- [x] X-002 출처별 구매금/당첨금/순이익/당첨률 계산 반영
- [x] X-003 `StatsScreen`에 `출처별 성과 비교` 카드 섹션 추가
- [x] X-004 회귀 테스트 추가(`StatsViewModelTest`: 출처별 집계/빈데이터 0값 유지)
- [x] X-005 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug`)

## Y. 통계 인사이트 확장 C04(2026-02-26 Cycle-11)
- [x] Y-001 `StatsViewModel`에 회차별 ROI 트렌드 모델(`RoiTrendPoint`) 추가
- [x] Y-002 선택 기간 기준 회차별 구매/당첨/순이익 집계 로직 추가(최대 최근 8회)
- [x] Y-003 `StatsScreen`에 `회차별 ROI 트렌드` 카드 추가(순이익 바 + 회차 메타)
- [x] Y-004 회귀 테스트 추가(`StatsViewModelTest`: ROI 트렌드 8개 제한/빈데이터 케이스)
- [x] Y-005 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug :wear:assembleDebug`)

## Z. 번호 직접 추가 다중 게임 UX 개선(2026-02-26 Cycle-12)
- [x] Z-001 `ManualAddViewModel`에 다중 게임 상태 추가(`pendingGames`, `repeatCount`)
- [x] Z-002 `현재 번호 1게임 추가`/`같은 번호 반복 추가` 액션 구현(최대 5게임)
- [x] Z-003 `번호 직접 추가` 화면에 추가 게임 목록/삭제/반복 카운트 UI 반영
- [x] Z-004 저장 로직 확장: 누적 게임을 A~E 슬롯으로 한 번에 저장
- [x] Z-005 회귀 테스트 추가(`ManualAddViewModelTest`: 다중 저장/반복 저장)
- [x] Z-006 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug`)

## AA. 번호생성 수동 편집 교체 로직 명시화(2026-02-26 Cycle-13)
- [x] AA-001 `NumberGeneratorViewModel` 수동 반영 API 확장(`replaceTargetNumber` 지원)
- [x] AA-002 교체 대상 유효성 검증 추가(잠금 번호/없는 번호 선택 시 에러 반환)
- [x] AA-003 `NumberGeneratorScreen`에 교체 대상 직접 선택 UI 반영(현재 번호 탭)
- [x] AA-004 화면 안내 문구/상태 텍스트 보강(선택 번호 + 교체 대상 표시)
- [x] AA-005 회귀 테스트 추가(`NumberGeneratorViewModelTest`: 교체 대상 지정/유효성 오류)
- [x] AA-006 품질 스냅샷 재검증(`./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug`)

## AB. UI 비주얼 폴리시 2차(2026-02-26 Cycle-14)
- [x] AB-001 "기본 앱 느낌" 원인 재진단(실폰트 미연동/fallback 중심) 문서 반영
- [x] AB-002 폴리시 2차 가이드 문서 추가(`27-ui-visual-polish-pack.md`)
- [x] AB-003 타이포 가이드 v1의 현재 상태/잔여 갭 업데이트(`26` 2장/12장)
- [x] AB-004 디자인/아이디어/백로그/우선순위 연동(`08`, `21`, `16`, `22`, `README`)
- [x] AB-005 실제 브랜드 폰트 리소스(`res/font`) 1차 도입(`app/src/main/res/font/*.ttf`)
- [x] AB-006 `Type.kt` fallback을 브랜드 폰트 체계로 교체(`app/src/main/java/com/weeklylotto/app/ui/theme/Type.kt`)
- [x] AB-007 Home/Result 카드 레이어(배경 질감/그림자) 2차 적용(`HomeScreen.kt`, `ResultScreen.kt`)
- [x] AB-008 Generator/Manage 아이콘 스타일 규격화 적용(`LottoVisualTokens.kt`, `NumberGeneratorScreen.kt`, `ManageScreen.kt`)
- [x] AB-009 전/후 스크린샷 2세트(Home/Result, Generator/Manage) 확보(`docs/assets/typography-approval/*.png`)
- [x] AB-010 접근성 1.3x + 저조도 대비 시각 QA 재검증(`docs/assets/visual-proof-matrix/*.png`, `docs/32-visual-proof-matrix-report.md`)

## AC. 루틴 점검 즉시 조치(2026-02-26 Cycle-15)
- [x] AC-001 코드 진행 스냅샷 재검증(`app/main=101`, `test=21`, `androidTest=5`, `wear/main=2`)
- [x] AC-002 품질 스냅샷 재검증(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AC-003 디자인 잔여 갭 확정(`Type.kt` 시스템 fallback 폰트 사용 상태)
- [x] AC-004 AB 우선순위 재확인(AB-005/006 최우선, AB-007/008 차순위)
- [x] AC-005 AB-005 착수 전 폰트 자산/라이선스 체크리스트 확정(`docs/31-font-assets-and-license-register.md`)

## AD. 루틴 점검 즉시 조치(2026-02-26 Cycle-16)
- [x] AD-001 코드/문서 워크트리 상태 재확인(문서 중심 변경, 코드 미수정)
- [x] AD-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AD-003 실폰트 미적용 상태 유지 확인(`Type.kt` fallback `SansSerif/Monospace`)
- [x] AD-004 AB 우선순위 유지 확인(`AB-005/006` 최우선)
- [x] AD-005 시크릿 JSON 처리 정책 결정(`.gitignore` 제외 + `14-signing-and-distribution.md` 운영 규칙 명시)

## AE. 루틴 점검 즉시 조치(2026-02-26 Cycle-17)
- [x] AE-001 코드 진행 스냅샷 재확인(`app/main=101`, `test=21`, `androidTest=5`, `wear/main=2`)
- [x] AE-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AE-003 디자인 잔여 갭 유지 확인(`Type.kt` fallback 폰트 상태)
- [x] AE-004 AB 우선순위 유지 확인(`AB-005/006` 최우선)
- [x] AE-005 시크릿 JSON 2종의 저장소 정책 확정(무시/로컬보관, Firebase 배포 스크립트 경로 명시)

## AF. 루틴 점검 즉시 조치(2026-02-26 Cycle-18)
- [x] AF-001 시크릿 JSON 제외 규칙 재검증(`.gitignore`)
- [x] AF-002 Firebase 배포 문서/스크립트 정합성 확인(`14-signing-and-distribution.md`, `scripts/firebase-distribute.sh`)
- [x] AF-003 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AF-004 디자인 잔여 갭 유지 확인(`Type.kt` fallback 폰트)
- [x] AF-005 Firebase 배포 운영 검증 완료(`docs/assets/distribution/firebase_dry_run_2026-02-26.md`, PR#1 merge 체인 기준 `Release Preflight`→`Firebase Distribution` 성공)

## AG. 루틴 점검 즉시 조치(2026-02-26 Cycle-19)
- [x] AG-001 코드 진행 스냅샷 재확인(`app/main=101`, `test=21`, `androidTest=5`, `wear/main=2`)
- [x] AG-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AG-003 시크릿 JSON 정책 완료 상태 재확인(`AD-005`, `AE-005`)
- [x] AG-004 디자인 잔여 갭 유지 확인(`Type.kt` fallback 폰트)
- [x] AG-005 AB-005 착수용 폰트 자산 패키지 확정(파일/라이선스/보관 경로, `docs/31-font-assets-and-license-register.md`)

## AH. 루틴 점검 즉시 조치(2026-02-26 Cycle-20)
- [x] AH-001 코드 진행 스냅샷 재확인(`app/main=101`, `test=21`, `androidTest=5`, `wear/main=2`)
- [x] AH-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AH-003 실폰트 fallback 상태 유지 재확인(`Type.kt`)
- [x] AH-004 시크릿 JSON 정책/배포 경로 유지 확인(`.gitignore`, `docs/14`, `scripts/firebase-distribute.sh`)
- [x] AH-005 AB-005/AG-005 통합 착수 체크리스트 확정(폰트 파일/라이선스/보관/적용 순서, `docs/30-font-onboarding-gate.md`)

## AI. 루틴 점검 즉시 조치(2026-02-26 Cycle-21)
- [x] AI-001 코드 진행 스냅샷 재확인(`app/main=101`, `test=21`, `androidTest=5`, `wear/main=2`)
- [x] AI-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AI-003 실폰트 fallback 상태 유지 재확인(`Type.kt`)
- [x] AI-004 우선순위 유지 확인(`AH-005` → `AF-005` → `AB-005/006`)
- [x] AI-005 AB-005 코드 착수 전 승인 기준 확정(체크리스트 + 배포 검증 증적 기준 문서화, `docs/30-font-onboarding-gate.md`)

## AJ. 루틴 점검 즉시 조치(2026-02-26 Cycle-22)
- [x] AJ-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AJ-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AJ-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AJ-004 착수 게이트 우선순위 재확인(`AH-005` → `AF-005` → `AB-005/006`, `AB-007/008`은 후순위)
- [x] AJ-005 AB-005 착수 승인 패키지 확정(체크리스트 + 배포 검증 증적 + 타이포 시안 2종, `docs/30`, `docs/assets/distribution/firebase_dry_run_2026-02-26.md`, `docs/assets/typography-variants/*.png`)

## AK. 루틴 점검 즉시 조치(2026-02-26 Cycle-23)
- [x] AK-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AK-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AK-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AK-004 우선순위 게이트 유지 확인(`AH-005` → `AF-005` → `AB-005/006`)
- [x] AK-005 AB-009/AB-010 시각 증적 매트릭스 확정(Home/Result/Generator/Manage × 1.0x/1.3x × 저조도, `docs/assets/visual-proof-matrix/*.png`, `docs/32-visual-proof-matrix-report.md`)

## AL. 루틴 점검 즉시 조치(2026-02-26 Cycle-24)
- [x] AL-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AL-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AL-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AL-004 우선순위 게이트 유지 확인(`AH-005` → `AF-005` → `AB-005/006`)
- [x] AL-005 `AK-005` 실행 운영안 확정(`27` 12장: 파일명 규칙/담당자/완료 판정 템플릿)

## AM. 루틴 점검 즉시 조치(2026-02-26 Cycle-25)
- [x] AM-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AM-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AM-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AM-004 `AL-005` 완료 상태 확인(`27` 12장 존재 + 규칙 3종 정의)
- [x] AM-005 `AJ-005` 산출물 인벤토리 확정(`28-approval-package-inventory.md` 생성, 경로/상태 매핑)

## AN. 루틴 점검 즉시 조치(2026-02-26 Cycle-26)
- [x] AN-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AN-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AN-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AN-004 `AM-005` 완료 상태 확인(`28` 인벤토리 기준 `ready/missing` 분류 완료)
- [x] AN-005 승인 패키지 `missing` 3종 해소 계획 확정(`29-missing-artifacts-recovery-plan.md`)

## AO. 루틴 점검 즉시 조치(2026-02-26 Cycle-27)
- [x] AO-001 코드 진행 스냅샷 재확인(`app/main=120`, `test=21`, `androidTest=5`, `wear/main=9`)
- [x] AO-002 품질 스냅샷 재확인(`./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug`)
- [x] AO-003 실폰트 fallback 상태 유지 재확인(`Type.kt`: `SansSerif`/`Monospace`)
- [x] AO-004 `AN-005` 계획 문서/인벤토리 연동 확인(`28` 5장, `29` 3~5장)
- [x] AO-005 `29` 실행 1차 착수 증적 확보(`distribution_evidence`를 `ready`로 전환, `docs/assets/distribution/firebase_dry_run_2026-02-26.md`)

## AP. 구매 리다이렉트 UX 고도화(2026-02-26 Cycle-40)
- [x] AP-001 사용자 요청 기반 문제 정의: "구매 경로 진입이 번거로움" 확인
- [x] AP-002 외부 이동 기본 정책 정의(공식 URL만 허용, 브라우저/커스텀탭 우선)
- [x] AP-003 UX 플로우 정의(CTA 탭 → 1회 안내 모달 → 외부 이동)
- [x] AP-004 실패 fallback 정의(열기 실패 시 `링크 복사`/`기본 브라우저로 열기`)
- [x] AP-005 적용 범위 확정: Home 1차 노출 + 이벤트 키 확정(`purchase_redirect_tap/confirm/fail/open_browser/copy_link`) 및 코드 반영

## AQ. 최근 조회 회차 기억(A03) 구현(2026-02-27 Cycle-41)
- [x] AQ-001 `ResultViewModel` 초기 진입 시 `ResultViewTracker`의 최근 조회 회차를 우선 복원
- [x] AQ-002 최근 조회 회차가 있는 경우 `fetchByRound` 경로로 우선 조회되도록 연동
- [x] AQ-003 우선순위/아이디어 문서에 `A03` 완료 상태 동기화(`21`, `22`) 및 회귀 테스트 추가(`ResultViewModelTest`)

## AR. 워치 컴플리케이션(D03) v1 구현(2026-02-27 Cycle-42)
- [x] AR-001 Wear 모듈에 complication data source 의존성 추가(`watchface-complications-data-source-ktx`)
- [x] AR-002 `WeeklyLottoComplicationService` 구현(SHORT/LONG_TEXT + 앱 실행 tap action)
- [x] AR-003 Manifest/문자열/우선순위·아이디어 문서 동기화(`AndroidManifest.xml`, `wear strings`, `21`, `22`)
