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
