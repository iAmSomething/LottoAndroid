# 모션/상호작용 고도화 플레이북 (v2)

## 1. 목적
- 스플래시 애니메이션과 앱 내부 상호작용을 제품 수준으로 통일한다.
- "예쁜 모션"이 아니라 "행동 유도 + 상태 전달 + 지각 성능 개선"을 목표로 한다.

## 2. 모션 원칙
- 빠름: 핵심 전환은 180~280ms, 길어도 400ms 이내
- 의미: 상태 변화가 있는 경우에만 모션 사용
- 일관: 동일 액션은 동일 이징/지속시간 사용
- 안전: 저사양/접근성 환경에서 축소 모드 제공

## 3. 스플래시 애니메이션 규격
## SPL-01 브랜드 인트로
- 구성: 배경 페이드 + 로고 스케일 인 + 텍스트 슬라이드 업
- 총 길이: 900ms 이내
- 스킵 규칙: 콜드 스타트 1회만 전체 재생, 웜 스타트는 축약(300ms)

## SPL-02 로딩 브리지
- 목적: 홈 데이터 준비 중 체감 지연 완화
- 구성: 회차 카드 skeleton + CTA placeholder
- 종료 조건: 홈 첫 데이터 수신 즉시 전환

## SPL-03 오류 브리지
- 초기 로딩 실패 시 "재시도" CTA 포함한 정적 상태로 전환
- 애니메이션 반복 금지(피로도/배터리 이슈 방지)

## 4. 내부 상호작용 규격
## INT-01 버튼/CTA
- Press feedback: scale 0.98, 80ms
- 완료 피드백: subtle highlight 140ms

## INT-02 번호볼(BallChip)
- 선택: fill/outline 전환 160ms
- 잠금: lock badge pop 200ms
- 적중: pulse 1회(240ms), 반복 금지

## INT-03 리스트/카드
- 진입: stagger 24ms 간격, 최대 6개 항목
- 삭제: collapse + fade 180ms
- 이동/복사 완료: inline confirmation chip 1.5초

## INT-04 시트/다이얼로그
- 바텀시트: 260ms up/ease-out
- 적용 버튼 클릭 시 즉시 닫힘 + 토스트/스낵 피드백

## INT-05 탭/네비게이션
- 탭 전환: content crossfade 180ms
- 하단바 아이콘 활성화: tint + scale 140ms

## 5. 접근성/성능
- 접근성 설정에서 "모션 축소" 모드 제공
- Reduce Motion 모드: duration 50% 축소, transform 최소화
- 배터리 보호: 반복 애니메이션/무한 루프 금지
- 목표: 프레임 드롭(>16ms) 비율 1% 이하

## 6. 계측 이벤트
- `motion_splash_shown` (cold/warm)
- `motion_splash_skip`
- `interaction_cta_press` (screen/component)
- `interaction_ball_lock_toggle`
- `interaction_sheet_apply`

## 7. QA 체크리스트
- 스플래시 총 길이 900ms 이내
- 웜 스타트 축약 모드 동작
- 탭 전환/시트/리스트 모션 일관성
- 모션 축소 모드에서 기능/가독성 저하 없음
- 저사양 디바이스에서 프레임 드롭 허용치 이내

## 8. 작업 보드 매핑
- 상세 작업은 `10-detailed-todo-board.md`의 `M-001 ~ M-020`으로 추적한다.

## 9. 베이스라인 진단 (2026-02-26)
- 코드 스냅샷 기준:
  - 핵심 화면/기능은 구현 범위가 넓지만, 모션 API 사용은 제한적
  - 스플래시 전용 애니메이션 흐름은 미적용 상태
- 우선 적용 순서:
  1. SPL-01/02(스플래시/로딩 브리지)
  2. INT-01/02(CTA + BallChip 상태 전이)
  3. INT-04/05(시트/탭 전환)
  4. Reduce Motion + 성능 검증

## 10. M-태스크 완료 매핑
| ID | 결정/산출 | 반영 위치 |
|---|---|---|
| M-001 | 콜드/웜/오류 스플래시 시나리오 정의 | 3장 SPL-01~03 |
| M-002 | 모션 토큰(duration/easing) 정의 | 2장 원칙, 3장/4장 수치 |
| M-003 | 홈 skeleton 전환 규칙 정의 | 3장 SPL-02 |
| M-004 | 스플래시 스킵/축약 정책 정의 | 3장 SPL-01 |
| M-005 | 버튼 press/release 피드백 정의 | 4장 INT-01 |
| M-006 | BallChip 상태 전이 모션 정의 | 4장 INT-02 |
| M-007 | 탭 전환 모션 정의 | 4장 INT-05 |
| M-008 | 시트/다이얼로그 모션 정의 | 4장 INT-04 |
| M-009 | 리스트 추가/삭제/정렬 전이 정의 | 4장 INT-03 |
| M-010 | 저장/복사/삭제 완료 피드백 정의 | 4장 INT-03, INT-04 |
| M-011 | 오류 피드백 강도 정책 정의 | 3장 SPL-03, 5장 접근성/성능 |
| M-012 | Reduce Motion 정책 정의 | 5장 접근성/성능 |
| M-013 | 모션 접근성 점검 체크리스트 정의 | 7장 QA 체크리스트 |
| M-014 | 모션 성능 목표(FPS/jank) 정의 | 5장 접근성/성능 |
| M-015 | 저사양 fallback 정책 정의 | 2장 원칙, 5장 접근성/성능 |
| M-016 | 모션 이벤트 로깅 스키마 정의 | 6장 계측 이벤트 |
| M-017 | EXP-05 실험 기준 정의 | `23-kpi-and-experiment-plan.md` EXP-05 |
| M-018 | EXP-06 실험 기준 정의 | `23-kpi-and-experiment-plan.md` EXP-06 |
| M-019 | 모션 QA 리포트 기준 정의 | 7장 QA 체크리스트 |
| M-020 | 릴리즈 게이트 반영 기준 정의 | 5장 성능/접근성 목표, 7장 QA |

## 11. Cycle-02 구현 매핑 (2026-02-26)

### 11.1 우선 적용 화면
- App 시작 구간: SPL-01/02(브랜드 인트로 + 로딩 브리지)
- Home: INT-01(핵심 CTA), INT-03(리포트 카드 진입)
- Number Generator: INT-02(BallChip 선택/잠금), INT-01(저장 CTA)
- Manage: INT-03(리스트 삭제/이동), INT-04(필터/정렬 시트)
- Result: INT-04(회차 선택 시트), INT-02(적중 상태 강조)
- 하단 내비게이션: INT-05(탭 전환/활성 아이콘)

### 11.2 이벤트 계측 포인트
- SPL-01/02: `motion_splash_shown`, `motion_splash_skip`
- Home/Generator/Manage 공통 CTA: `interaction_cta_press`
- BallChip 상태 전이: `interaction_ball_lock_toggle`
- 시트 적용 액션: `interaction_sheet_apply`

### 11.3 현재 코드 갭
- 모션 스펙은 문서화되었지만, 실제 `AnimatedVisibility/Crossfade/AnimatedContent` 적용 범위는 제한적
- 다음 루틴에서 코드 반영 증적(화면별 적용 위치 + 테스트/스크린샷)을 반드시 남긴다

## 12. Cycle-03 계측 상태 업데이트 (2026-02-26)

### 12.1 연결 완료
- `motion_splash_shown`: SplashGate 연결(cold/warm)
- `motion_splash_skip`: SplashGate warm compact 경로 연결
- `interaction_cta_press`: Home/Generator/Manage/Result 주요 CTA 경로 연결
- `interaction_ball_lock_toggle`: Number Generator 잠금 토글 경로 연결
- `interaction_sheet_apply`: Manage/Result 시트 적용 경로 연결

### 12.2 미연결
- 없음(현재 문서 정의 이벤트 기준 전부 연결)

### 12.3 다음 적용 규칙
- 스플래시 composable이 들어가는 PR에서는 `motion_*` 2종 이벤트 연결을 필수 게이트로 처리한다.
  - 자동 체크: `scripts/check-splash-motion-gate.sh`
  - CI 연동: `.github/workflows/android-ci.yml`, `.github/workflows/release-preflight.yml`
- `motion_*`, `interaction_*` 모두 공통 파라미터 키(`screen`, `component`, `action`)를 유지하고, 값은 enum으로 고정한다.

## 13. Cycle-07 코드 반영 (2026-02-26)

### 13.1 Reduce Motion 실제 적용
- 설정 경로:
  - `SettingsScreen`에 `모션 축소` 토글 추가
  - `SettingsViewModel`에서 즉시 저장
  - `DataStoreMotionPreferenceStore`로 영속화
- 앱 전역 적용:
  - `MainActivity`에서 reduce motion 값을 observe
  - `LocalMotionSettings`를 통해 화면/컴포넌트에 전달

### 13.2 모션 2차 적용 컴포넌트
- `SplashGate`
  - 모션 축소 시 duration 50% 적용
  - scale transform 제거(페이드 중심)
- `LottoBottomBar`
  - 탭 활성/비활성 tint + alpha + scale 피드백 적용
  - 모션 축소 시 scale 생략
- `BallChip`
  - 상태 전환 색상 애니메이션(배경/테두리/텍스트)
  - `HIT/LOCK` 상태 scale 피드백 적용
  - 모션 축소 시 scale 생략

### 13.3 이벤트 action 값 enum 고정
- `AnalyticsActionValue`를 도입해 `click/apply/lock/unlock/cold/warm/compact`를 상수화
- Home/Generator/Manage/Result/Splash에서 리터럴 제거

## 14. Cycle-08 코드 반영 (2026-02-26)

### 14.1 INT-01 버튼/CTA press feedback 확대
- 공통 모디파이어 `motionClickable` 도입
  - pressed 시 scale `0.98`
  - Reduce Motion 활성 시 transform 생략
- 적용 경로:
  - Home CTA 카드(QR/번호생성/미확인결과/전체보기)
  - Result 회차 변경 텍스트 액션
  - `TicketCard` 클릭 경로 공통화

### 14.2 INT-04 시트 선택행 press feedback 보강
- Result 회차 변경 시트의 라운드 선택 행에 `motionClickable` 적용
- 선택 + press 피드백을 함께 제공해 터치 확인성을 강화

### 14.3 INT-03 리스트/카드 전이 보강
- Home/Manage의 티켓 카드 및 요약 카드에 `Modifier.animateItem` 적용
- 목록 변경/정렬 시 카드 배치 전이를 자연스럽게 처리
