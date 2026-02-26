# 진행 추적 로그

## 업데이트 규칙
- 날짜 형식: `YYYY-MM-DD`
- 필수 항목: 완료 작업, 미완료 작업, 블로커, 다음 액션

## 2026-02-25
- 완료 작업
  - Android 프로젝트 스캐폴딩 완료
  - 도메인/데이터/화면 기본 흐름 구현
  - 문서 00~08 작성 완료
  - `assembleDebug`, `testDebugUnitTest`, `ktlintCheck`, `detekt` 통과
  - QR 실시간 스캔(CameraX+ML Kit) 1차 구현
  - 실물 복권 사진 기반 스캔 가이드 문구 반영
  - QR 실샘플 회귀 테스트 케이스 추가
  - 알림 설정 DataStore 영속화 구현
  - 다중 티켓 연속 스캔 모드 구현
  - 부팅 후 알림 자동 복구(`BOOT_COMPLETED`) 구현
  - QR 스캔 가이드 오버레이 UI 구현
  - 데이터 변경 시 위젯 강제 갱신 트리거 구현
  - 당첨 API 에러 분기별 사용자 메시지 표준화
  - 위젯 액션의 앱 딥링크 진입 구현
  - 회차 추정 로직 테스트 케이스 보강
  - 위젯 정보 밀도(라벨/요약/액션) 개선
  - 통계의 누적 당첨금을 실제 채점 합산으로 연동
  - 통계 기간 필터(전체/최근 4주) 추가
  - Compose UI 스모크 테스트 추가
  - Room + Repository 통합 테스트 추가
  - 번호 생성 수동입력 오류 메시지 세분화 및 숫자 키보드 UX 적용
  - 생성번호 저장 중복 정책 고도화(동일 회차 `GENERATED` 덮어쓰기, QR/수동 누적)
  - QR 스캔 실패 누적 가이드/재시도 UX 추가
  - NumberGeneratorViewModel 단위 테스트 추가
  - RoomTicketRepository 통합 테스트에 저장정책 회귀 케이스 추가
  - `compileDebugAndroidTestKotlin` 포함 품질 게이트 재통과
  - AGP `8.5.2` → `8.8.2` 상향(compileSdk 35 경고 해소)
  - 알림 설정 화면에 Android 13+ 알림 권한 런타임 요청 반영
  - 릴리즈 서명 설정 추가(`keystore.properties`/환경변수 지원)
  - 릴리즈 문서 12~14 추가(스토어 메타/권한/서명 배포)
  - 디자인 참고문서(`/Volumes/무제/design_spec_android_lotto.md`) 기준 UI 구조 1차 정렬
  - BottomTab을 Home/Manage/Result 3탭으로 재구성
  - Manage 화면(편집모드/필터/삭제/FAB 시트) 및 ManualAdd/Import/TicketDetail 라우트 추가
  - 디자인 토큰/공통 컴포넌트(`LottoTopAppBar`, `LottoBottomBar`, `BallChip`, `TicketCard`, `StatusBadge`) 적용
  - `assembleRelease` 포함 빌드 재검증 통과
  - Result 회차 피커를 실제 회차 데이터 스위칭(`fetchByRound`)으로 연동
  - Generator 화면 상단/컬러를 토큰 기반으로 정렬하고 `전체 초기화` 동작 분리
  - QR/설정 화면 상단을 `LottoTopAppBar` 기반으로 통일
  - Home/Result 번호 표시를 `BallChip`/`TicketCard` 중심으로 재정렬
  - QR 인식 후 즉시 저장 대신 `저장 확인 시트(취소/저장)` 단계 추가
  - J-007 픽셀 보정: Home/Manage/Result/Generator 간격·타이포·카드 라운드·배지 높이 재정렬
  - Manage 필터 시트에 회차 범위(전체/최근5회/최근10회) 옵션 추가
  - 상태/출처 라벨 한국어화(대기/당첨/낙첨, 자동/수동/QR) 및 티켓 날짜 포맷 적용
  - `ktlintFormat` 후 `ktlintCheck/detekt/testDebugUnitTest/assembleDebug` 재통과
  - 에뮬레이터 부팅 완료 후 `connectedDebugAndroidTest` 재실행 4/4 통과(재실행 포함)
  - `MainNavigationInstrumentedTest` 추가 후 `connectedDebugAndroidTest` 5/5 통과
  - 변경 반영 후 `assembleRelease` 재검증 통과
  - QR 수동 입력 계측 테스트(`QrManualFlowInstrumentedTest`) 추가
  - 설정/위젯/통계 단위 테스트(`SettingsViewModelTest`, `DefaultWidgetDataProviderTest`, `StatsViewModelTest`) 추가
  - 전체 계측 테스트 7/7 통과
  - 당첨 API 이슈 수정: 공식 endpoint 차단(302/error.html) 시 미러 endpoint fallback 추가
  - 당첨 결과 화면 실기동 확인: `1212회` 당첨번호/보너스 정상 렌더링
  - 스토어 스크린샷 세트 갱신(`docs/assets/store-screenshots/home|manage|result|generator|qr_scan|stats.png`)
  - 버전 상향(`versionCode=2`, `versionName=0.2.0`)
  - 릴리즈 프리플라이트 스크립트 추가(`scripts/release-preflight.sh`)
  - 프리플라이트 실행 리포트 작성(`17-release-preflight-report.md`, PASS 11 / WARN 1 / FAIL 0)
  - 로컬 릴리즈 서명 자동화 스크립트 추가(`scripts/setup-local-release-signing.sh`)
  - 로컬 릴리즈 키/백업 생성 및 설정 정규화 완료
  - 프리플라이트 최종 재실행 결과 갱신(PASS 13 / WARN 0 / FAIL 0)
  - CI 릴리즈 프리플라이트 워크플로우 추가(`.github/workflows/release-preflight.yml`)
  - preflight 스크립트에 CI 모드(`--with-build-ci`, `--skip-adb`, `--require-signing`) 추가
  - 워크플로우 이벤트 분리: PR(서명 비필수), Push/수동(서명 필수)
  - GitHub Secrets 동기화 스크립트 추가(`scripts/sync-gh-release-secrets.sh`)
  - 시크릿 동기화 스크립트 안전모드 보강(기본 dry-run, `--apply` 명시 업로드)
  - 시크릿 동기화 스크립트에 Android 저장소 구조 검증(`app/build.gradle.kts`) 추가
  - 비대상 repo(`iAmSomething/LotteryPicker`) 업로드 차단 검증 및 기존 `LOTTO_RELEASE_*` 시크릿 정리 완료
  - `--repo` 미지정 시 계정 Android repo 자동 탐지 로직 추가(0/복수 후보면 안전 중단)
  - `ktlintCheck`, `testDebugUnitTest`, `connectedDebugAndroidTest(7/7)`, `assembleRelease` 재통과
  - 운영 인수인계 문서 추가(`15-incident-response-runbook.md`, `16-next-sprint-backlog.md`)
  - 주중 회차 불일치 버그 수정: 홈/번호관리/저장 기준을 `다음 추첨(판매) 회차`로 통일
  - `RoundEstimator.nextDrawDate`, `RoundEstimator.currentSalesRound` 추가 및 회귀 테스트 보강
  - 회귀 검증 재실행: `testDebugUnitTest` + `connectedDebugAndroidTest`(선별 4/4) 통과
  - GitHub Actions `Release Preflight` 성공 확인(run `22419063335`)
  - 번호 생성 저장 흐름 계측 회귀 테스트 추가(`WeeklySaveFlowInstrumentedTest`)
  - 품질 게이트 재검증: `ktlintCheck`, `detekt`, `testDebugUnitTest`, `connectedDebugAndroidTest`(8/8) 통과
  - Figma MCP 원본 대조 재시도 결과: 플랜 호출 한도 응답으로 직접 노드 수집 여전히 불가
  - 디버그 전용 위젯 갱신 이력 로깅 추가(`WidgetRefreshHistoryLogger`, `widget_refresh_history.log`)
  - 위젯별 예외 격리 적용: 한 위젯 갱신 실패 시 다른 위젯 갱신 계속 수행
  - 위젯 로그 조회 스크립트 추가(`scripts/show-widget-refresh-history.sh`)
  - Result 네트워크 실패 UX 개선: 자동 재시도(최대 3회), 실패 시각 노출, 최신 회차 재조회 액션 추가
  - ResultViewModel 재시도 로직 단위 테스트 추가(`ResultViewModelTest`)
  - 품질 게이트 재검증: `ktlintCheck`, `detekt`, `testDebugUnitTest`, `connectedDebugAndroidTest`(8/8) 통과
  - QR 저조도/반사 대응 UX 강화: 플래시 토글, 환경 가이드 시트, 실패 누적 시 권장 액션 추가
  - 수동 입력 시 카메라 스캐너 자동 일시정지 처리(입력 간섭 방지)
  - QR 수동입력 계측 테스트를 태그 기반 상호작용 검증으로 안정화(`QrManualFlowInstrumentedTest`)
  - 통계 기간 필터를 `전체/최근4주/최근8주`로 확장하고 8주 경계 회귀 테스트 추가(`StatsViewModelTest`)
  - 실기기 계측 검증 자동화 스크립트 추가(`scripts/run-physical-device-validation.sh`) 및 이력 문서 추가(`18-device-validation-report.md`)
  - 실기기 미연결 상태에서 스크립트 실패 가드 확인(`[FAIL] No physical device connected`)
  - 공통 컴포넌트 접근성 보강: 번호볼 스크린리더 문구, 클릭 가능한 카드/상단 액션 버튼 role 적용
  - 접근성 문구 회귀 단위 테스트 추가(`BallChipAccessibilityTest`)
  - Ticket 상세 화면 공유 버튼 추가 및 시스템 공유 인텐트 연동
  - 공유 텍스트 포맷터 추가(회차/출처/상태/게임 번호) 및 단위 테스트(`TicketDetailShareFormatterTest`)
  - 테마 색상 콘트라스트(WCAG AA 4.5:1) 자동 검증 테스트 추가(`ColorContrastTest`)
  - 앱 아이콘 브랜딩 리뉴얼: adaptive icon foreground/background 개선 + Android 13 themed icon용 monochrome 추가
- 미완료 작업
  - 실제 디바이스 1대 기준 계측 테스트 추가 검증
  - Figma 원본 노드 기준 정밀 픽셀 매핑(현재 MCP 호출 한도 이슈로 대기)
- 블로커
  - Figma MCP 호출 한도(노드 직접 대조 불가)
- 다음 액션
  - 실기기 1대에서 `connectedDebugAndroidTest` 1회 추가 검증
  - Figma 툴 호출 가능 상태에서 node `6:2` 기준 화면 밀도/간격 최종 동기화

## 상태 요약
| 영역 | 상태 | 비고 |
|---|---|---|
| 기반/환경 | Green | AGP 8.8.2 상향 완료 |
| 번호 생성 | Green | 주요 기능 동작 |
| QR 등록 | Green | 실스캔/실샘플/오버레이 반영 완료 |
| 당첨 결과 | Green | 에러 분기별 UX 표준화 완료 |
| 알림 | Green | 영속화 + 부팅 복구 완료 |
| 위젯 | Green | 갱신 트리거/딥링크/정보 밀도 개선 완료 |
| 통계 | Green | 당첨금 연동 + 기간 필터 완료 |
| 테스트 품질 | Green | 정적검사/단위/계측(AVD) 통과 |
| 디자인 정렬 | Yellow | 스펙 기반 픽셀 보정 완료, Figma 노드 직접 대조만 대기 |
