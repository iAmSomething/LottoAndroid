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
- 번호 구간 분포 히트맵 계산(1-9/10-19/20-29/30-39/40-45 count/percent)
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
- 크래시/ANR 자동 분류 템플릿 검증: 카테고리/심각도/owner/즉시조치 매핑 리포트 생성 검증
- 자동 검증: `run-ops-observability-check.sh`, `verify-analytics-events.sh --profile ops-core`, `evaluate-ops-observability-threshold.sh`
- 자동 검증(보강): `classify-crash-anr-template.sh --log-file <path> --report-file <path>`

### 예외처리/안정성 하드닝
- 네트워크 오류 타입별 사용자 메시지/재시도 정책 검증(timeout/4xx/5xx/schema/unknown)
- DB 예외 시나리오 검증(저장공간 부족/쓰기 실패/마이그레이션 실패)
- 외부 링크 리다이렉트 실패 fallback 검증(링크 복사/기본 브라우저 열기)
- 오류 안내 1초 노출/2탭 fallback 도달 검증(`SettingsPurchaseRedirectInstrumentedTest`, `ExternalOpenFallbackDialogInstrumentedTest`)
- 프로세스 kill 후 핵심 화면 상태 복원 검증(Home/Result/Manage)
- 사용자 여정 리허설 세트 검증(`J01~J06` 중 최소 4개, 주간 루틴)
- 계층별 구현 슬라이스 검증(`S07-1~S07-3`, 오류 키/메시지/로그/공통 UI 재사용)

### 성능 최적화
- 콜드 스타트 시간 샘플 검증(P95 목표치 비교)
- 리스트 스크롤 jank 비율 샘플 검증
- ANR/Crash-free 지표 추적 로그 검증
- 저사양 기기 메모리 압박 fallback 동작 점검
- 핫패스 우선 구간(Home/Result/Manage) 프로파일링 기반 회귀 검증
- 성능 수집 파이프라인/게이트 검증(`S07-4`, 자동 수집 결과의 릴리즈 판정 연동)
- 성능 게이트 캘리브레이션 검증(`S08`, emulator/device 분리 + 반복측정 N=5 + warm-up 제외)
- 성능 게이트 운영 템플릿 검증(`S09`, 커맨드 매트릭스/리포트 필수 필드/판정 트리 적용)
- 성능 게이트 증적 패키지 검증(`S10`, E1~E5 구성/최종 결론/후속 액션 연결)

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
- `connectedDebugAndroidTest` 통과(최신 S11 게이트 실행 8 tests, 0 failures)
- Wear OS 핵심 플로우 회귀 테스트 통과(추가 시나리오 기준)
- 모션 품질 기준 통과(스플래시 시간/Reduce Motion/프레임 안정성)
- 치명 이슈(P0/P1) 0건
- UI 품질 게이트 검증(`S11`): U1~U4(타이포/비주얼/상호작용/접근성) PASS + U5 성능 연동 판정 일치 확인
- 통합 결론 검증(`S12`): `S10`/`S11` 결론 충돌 시 규칙(R1~R4) 적용 결과가 단일 결론과 일치하는지 확인
- 최신 증적: `docs/assets/distribution/ui_quality_gate_evidence_2026-02-26.md`, `docs/assets/distribution/unified_verdict_2026-02-26.md`
- 드라이런/에스컬레이션 검증(`S13`): 최근 2개 빌드 기준 드라이런 결과와 E13 코드, SLA 기록 누락 여부 확인
- 이력/추세 검증(`S14`): 주간 리포트의 결론 수치/리드타임/완료율이 원본 드라이런 레코드와 일치하는지 확인
- 위험예산/프리즈 검증(`S15`): 주간 소진율 계산, 프리즈 트리거, 해제 조건, 예외 승인 기록 정합성 확인
- 프리즈 지휘/커뮤니케이션 검증(`S16`): 발동 공지(10분), 주기 업데이트(2시간), 해제 회의(15분) 및 사후 액션 이관 여부 확인
- 프리즈 드릴/준비도 검증(`S17`): 시나리오별 점수카드 산출, 임계치 판정, 보정 액션 이관 정합성 확인
- 최신 운영 증적: `unified_verdict_risk_budget_2026-w09.md`, `freeze_command_log_2026-02-26.md`, `freeze_drill_scorecard_2026-02-26.md`
- 하드닝 실행 증적(추가): `corrective_action_loop_2026-w09.md`, `debt_release_block_2026-w09.md`, `anomaly_escalation_2026-02-26.md`, `capacity_coverage_2026-02-26.md`
- S06 핫패스 증적: `hotpath_s06_profile_2026-02-26.md`, `performance_gate_emulator_s06_2026-02-26.md`, `HotpathRenderLatencyInstrumentedTest`
- BK 실행 자동화: `scripts/run-bk-device-gate.sh` (실기기 연결 시 BK-001/BK-002 연속 실행)
- BK 대기형 자동화: `scripts/run-bk-when-physical.sh` (실기기 연결까지 대기 후 자동 실행)
- P-004 Wear 실행 자동화: `scripts/run-p4-wear-proof-gate.sh` (소형/대형 실기기 2종 증적 수집)
- P-004 Wear 대기형 자동화: `scripts/run-p4-when-wear-physical.sh` (실기기 2종 연결까지 대기 후 자동 실행)
- 릴리즈 래퍼 연동: `release-final-check.sh` 실행 시 Wear `P-004` probe를 자동 실행해 blocked/PASS 증적을 갱신
- 물리 게이트 체크포인트: `scripts/run-physical-gates-checkpoint.sh`로 `BK-001/002 + P-004` 상태를 단일 리포트로 동기화
- 물리 게이트 오케스트레이터: `scripts/run-all-physical-gates-when-ready.sh`로 폰 실기기 + Wear 2종 연결을 대기 후 `BK/P-004/checkpoint`를 순차 자동 실행
- 물리 게이트 루틴 점검: `scripts/run-physical-gates-routine-check.sh`로 blocked/PASS 상태와 증적 패키지를 1회 생성
- Wear 실기기 준비 체크리스트: `docs/assets/distribution/wear_physical_device_readiness_checklist_2026-02-27.md`
- 드릴 보정 액션 폐쇄 루프 검증(`S18`): WARN/FAIL 액션의 SLA 준수, 폐쇄 증적, 재개방 규칙 적용 정합성 확인
- 보정 액션 부채/릴리즈 차단 검증(`S19`): 액션 부채 점수 계산, 릴리즈 차단/해제 조건, 예외 승인 정합성 확인
- 보정 액션 부채 이상징후/자동 에스컬레이션 검증(`S20`): debt 급증 탐지, 자동 경보 단계, 에스컬레이션 응답 SLA 준수 여부 확인
- 에스컬레이션 대응 용량/커버리지 검증(`S21`): 시간대별 커버리지, 동시 경보 처리 한도, 핸드오버 체크리스트 준수 여부 확인
- UI 미감/타이포 완성도 검증(`S22`): A1~A5 scorecard PASS + Home/Result/Generator/Manage 전후 시각 증적 세트 확보 여부 확인
- 상태 기반 UI 내러티브/마이크로카피 검증(`S23`): N1~N5 scorecard PASS + loading/empty/error/offline/redirect 상태 매트릭스 증적 세트 확보 여부 확인
- 상태 전환 코레오그래피/피드백 아이덴티티 검증(`S24`): C1~C5 scorecard PASS + enter/steady/exit 전환 증적과 feedback layer(visual/haptic/copy) 일치 여부 확인
- 적응형 레이아웃/시선 흐름 검증(`S25`): L1~L5 scorecard PASS + 소형/표준/대형 화면에서 CTA 발견성/정보 밀도/엄지 영역 접근성 증적 세트 확보 여부 확인
- 시각 자산 일관성/로딩 fallback 검증(`S26`): V1~V5 scorecard PASS + 아이콘/이미지/배지 로딩/실패 fallback/오프라인 복원 증적 세트 확보 여부 확인
- 컨텍스트 우선순위/집중 모드 검증(`S27`): F1~F5 scorecard PASS + 기본/집중 모드 비교, 알림 억제, CTA 재배치, 복귀 흐름 증적 세트 확보 여부 확인
- 상호작용 신뢰 신호/실행 확인 검증(`S28`): R1~R5 scorecard PASS + 탭 후 즉시 상태 변화/완료 확인/실패 복구 경로 증적 세트 확보 여부 확인
- 의사결정 신뢰도/점진적 공개 UX 검증(`S29`): D1~D5 scorecard PASS + 요약->세부 공개 흐름/위험 신호 타이밍/결정 완료 동선 증적 세트 확보 여부 확인
- 외부 리다이렉트 연속성/복귀 컨텍스트 검증(`S30`): X1~X5 scorecard PASS + 이동 전 안내/실패 fallback/복귀 상태 복원 증적 세트 확보 여부 확인
- 지각 지연/로딩 연속성 검증(`S31`): P1~P5 scorecard PASS + 로딩 전/중/후 전환, timeout/retry, stale->fresh 복원 증적 세트 확보 여부 확인
- 세션 복귀/중단 복원 검증(`S32`): R1~R5 scorecard PASS + 백그라운드/외부 이동/kill 후 복귀 위치 및 입력 보존, 실패 fallback 증적 세트 확보 여부 확인
- 멀티디바이스 상태 동기화/충돌 복구 검증(`S33`): M1~M5 scorecard PASS + 폰/워치/위젯 상태 일치, 충돌 해결 로그, 오프라인 재접속 복원 증적 세트 확보 여부 확인
- 알림 피로도/빈도 개인화 검증(`S34`): N1~N5 scorecard PASS + 중복 억제, 조용한 시간 준수, 설정 변경 즉시 반영/복구 증적 세트 확보 여부 확인
- 권한 프릭션/점진적 동의 검증(`S35`): C1~C5 scorecard PASS + 맥락 기반 권한 요청, 거부/재요청/설정 복구, 대체 경로 증적 세트 확보 여부 확인
- 실험 플래그/점진 롤아웃 안전 검증(`S36`): F1~F5 scorecard PASS + 분기 결정 안정성, rollout/rollback 로그, 분기 KPI/에러 지표 증적 세트 확보 여부 확인
- 첫 주 온보딩/활성화 연속성 검증(`S37`): O1~O5 scorecard PASS + D0~D7 첫 가치 도달, 온보딩-실사용 연계, 재방문 재개 증적 세트 확보 여부 확인
- 검색/필터 발견성 및 질의 복원 검증(`S38`): Q1~Q5 scorecard PASS + 검색 진입 발견성, 빈 결과 대체 제안, 앱 복귀/재진입 질의 복원 증적 세트 확보 여부 확인
- 번호 기록 입력 정확성/중복 방지 검증(`S39`): 오입력 차단, 동일 기록 중복 감지/병합, 저장 실패 후 복원/재시도 증적 세트 확보 여부 확인
