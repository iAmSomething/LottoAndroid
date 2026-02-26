# 테스트 전략 / 시나리오

## 1. 테스트 레벨
- Unit Test: 번호 생성, QR 파싱, 채점 로직
- Integration Test: Repository + Room + API fallback
- UI Test: 홈/하단탭/생성 진입/결과 화면 핵심 흐름
- Manual Regression: 위젯/알림/오프라인

## 2. 핵심 케이스
### 번호 생성
- 6개 고유번호 생성
- 1~45 범위 검증
- 잠금 유지 + 잠금 제외 재생성 검증
- 자동 검증: `NumberGeneratorViewModelTest`, `MainNavigationInstrumentedTest`

### QR 파싱
- 정상 query 포맷
- compact `v` 포맷
- 누락/오류 포맷 실패 처리
- 자동 검증: `QrTicketParserTest`, `QrManualFlowInstrumentedTest`

### 당첨 판정
- 1~5등, 낙첨 전체 분기
- 2등 보너스 조건 검증
- 결과 조회 네트워크 실패 시 자동 재시도/최종 에러 상태 검증
- 자동 검증: `DefaultResultEvaluatorTest`, `ResultViewModelTest`

### 알림
- 기본 스케줄 예약
- 커스텀 스케줄 재예약
- 앱 재시작/기기 재부팅 후 동작
- 자동 검증: `SettingsViewModelTest`

### 위젯
- 번호 변경/결과 업데이트 이후 위젯 데이터 반영
- 자동 검증: `DefaultWidgetDataProviderTest`

### 오프라인
- API 실패 시 캐시 fallback
- 공식 API 차단/302 응답 시 미러 API fallback
- 사용자 메시지/재시도 버튼 노출

### 통계
- 누적 구매금/당첨금 계산
- 최근 4주 필터 계산
- 최근 8주 필터 계산(4주 초과/8주 이내 데이터 포함)
- 자동 검증: `StatsViewModelTest`

### 접근성
- 번호볼 상태별 스크린리더 문구 검증(일반/잠금/보너스)
- 클릭 가능한 공통 UI 요소의 역할(role) 검증
- 테마 핵심 색상 대비비율(WCAG AA 4.5:1) 검증
- 자동 검증: `BallChipAccessibilityTest`, `ColorContrastTest`

### 번호관리 상세
- 상세 화면 공유 텍스트 포맷(회차/출처/상태/게임 번호) 검증
- 자동 검증: `TicketDetailShareFormatterTest`

## 3. 합격 기준
- 핵심 유즈케이스 회귀 실패 0건
- CI 빌드/단위 테스트 성공
- `connectedDebugAndroidTest` 통과(현재 7 tests, 0 failures)
- `connectedDebugAndroidTest` 통과(현재 8 tests, 0 failures)
- 치명 이슈(P0/P1) 0건
