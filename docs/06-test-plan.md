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
- 번호 직접 추가 저장 시 중복 감지 후 선택지(취소/중복 제외 저장/중복 포함 저장) 검증
- 번호 직접 추가 저장 실패 시 초안/대기 게임 유지 및 재시도 문구 노출 검증
- 번호 직접 추가 저장 직후 실행 취소(undo) 동작 및 삭제 반영 검증
- 번호 직접 추가 저장 버튼 연속 탭 시 저장 요청 1회 처리(debounce) 검증
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
- 로컬 티켓 백업 파일 생성/복원(파일 부재 실패 처리 포함)
- 구매 리다이렉트 fallback(홈/결과/설정: 브라우저 재시도/링크 복사) 동작 검증

### 통계
- 누적 구매금/당첨금 계산
- 최근 4주 필터 계산
- 최근 8주 필터 계산(4주 초과/8주 이내 데이터 포함)
- 커스텀 회차 필터 계산(시작/끝 범위, 역전 입력 오류 처리)
- 조합 중복도 경고 계산(중복 비율/최다 반복 조합/경고 레벨)
- 출처별 성과 ROI% 계산(자동/수동/QR)
- 회차별 ROI 트렌드 ROI% 계산
- 자동 검증: `StatsViewModelTest`

### 접근성
- 번호볼 상태별 스크린리더 문구 검증(일반/잠금/보너스)
- 클릭 가능한 공통 UI 요소의 역할(role) 검증
- 테마 핵심 색상 대비비율(WCAG AA 4.5:1) 검증
- 자동 검증: `BallChipAccessibilityTest`, `ColorContrastTest`

### 번호관리 상세
- 상세 화면 공유 텍스트 포맷(회차/출처/상태/게임 번호) 검증
- 상태/출처/모드 라벨 매핑 일관성 검증
- 자동 검증: `TicketDetailShareFormatterTest`, `LottoUiLabelsTest`

### Wear OS
- 원형 화면 레이아웃 가독성(핵심 정보 첫 노출) 검증
- 워치 Home/Numbers/Result/Settings 내비게이션 회귀 검증
- 워치→폰 핸드오프 액션 성공/실패 분기 검증

### 실험/지표
- EXP-01/02/03/04 이벤트 로깅 정확도 검증
- 실험군/대조군 분기 규칙 검증
- KPI 집계 배치(주간) 누락/중복 검증

### 운영 관측성
- API 요청 관측 이벤트(`ops_api_request`)의 source/round/latency/status/error_type 스키마 검증
- 저장소 mutation 관측 이벤트(`ops_storage_mutation`)의 operation/latency/status 스키마 검증
- 임계치 자동 판정: official failure rate/terminal failure rate/API p95/storage failure rate/storage p95 기준 PASS/FAIL 검증
- 자동 검증: `run-ops-observability-check.sh`, `verify-analytics-events.sh --profile ops-core`, `evaluate-ops-observability-threshold.sh`

### 모션/상호작용
- 스플래시 콜드/웜 실행 시간 검증(목표 900ms 이하/축약 300ms)
- 핵심 인터랙션 모션 일관성 검증(버튼/볼/탭/시트/리스트)
- Reduce Motion 모드 동작 검증(축소 규칙 준수)
- 저사양 기기 프레임 드롭 허용치 검증(jank 비율)

### 플래키 감시
- 단위 테스트 게이트 반복 실행 감시(`scripts/run-unit-flaky-guard.sh --repeat N`)
- 주간 CI 루틴 감시(`.github/workflows/unit-flaky-guard.yml`)
- 리포트 필수 필드: 반복 횟수, PASS/FAIL 카운트, 실행별 duration, 로그 경로

## 3. 테스트 더블 네이밍/스코프 규칙
- 테스트 더블 클래스는 파일 단위 접두사로 시작한다. 예: `HomeFakeDrawRepository`, `ResultViewModelFakeResultViewTracker`
- 동일 패키지 내 공용 이름(`FakeRepository`, `AlwaysFifthEvaluator`)은 금지한다.
- 테스트 더블은 기본적으로 해당 테스트 파일 내부 `private` 선언으로 한정한다.
- 여러 테스트 파일에서 재사용해야 할 경우에만 `testFixtures` 또는 `testutil` 패키지로 이동하고, 도메인별 접두사를 유지한다.
- `object` 더블은 파일 내부 고유 목적일 때만 사용하고, 재사용 가능성이 있으면 `class`로 분리한다.

## 4. 합격 기준
- 핵심 유즈케이스 회귀 실패 0건
- CI 빌드/단위 테스트 성공
- `connectedDebugAndroidTest` 통과(현재 9 tests, 0 failures)
- Wear OS 핵심 플로우 회귀 테스트 통과(추가 시나리오 기준)
- 모션 품질 기준 통과(스플래시 시간/Reduce Motion/프레임 안정성)
- 치명 이슈(P0/P1) 0건
