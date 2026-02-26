# KPI 및 실험 계획 (2026-Q2)

## 1. 목적
- 고도화 아이디어를 "기능 출시"가 아니라 "지표 개선" 중심으로 검증한다.

## 2. 핵심 KPI
- 주간 활성 사용자(WAU)
- 4주 리텐션(설치 후 4주차 재방문율)
- 주간 루틴 완료율(번호 저장 + 결과 확인)
- 알림 클릭률(구매/결과)
- 결과 화면 진입률(저장 사용자 대비)
- 크래시 없는 세션 비율

## 3. KPI 목표치 (초안)
- WAU: +20%
- 4주 리텐션: +8%p
- 주간 루틴 완료율: +15%
- 알림 클릭률: +10%p
- 크래시 없는 세션: 99.5% 이상

## 4. 실험 카드
## EXP-01 미확인 결과 배지
- 가설: 미확인 상태를 홈에 강조하면 결과 확인 진입률이 증가한다.
- 대상: 최근 2주 내 티켓 저장 사용자
- 성공 기준: 결과 화면 진입률 +12% 이상

## EXP-02 주간 리포트 카드
- 가설: 주간 요약 카드는 주간 재방문을 늘린다.
- 대상: 알림 활성 사용자
- 성공 기준: WAU +6% 이상, 이탈률 증가 없음

## EXP-03 Wear 홈/결과 요약
- 가설: 워치 노출은 결과 확인 루틴 완료율을 높인다.
- 대상: Wear OS 연결 사용자
- 성공 기준: 루틴 완료율 +10% 이상

## EXP-04 번호관리 빠른 액션
- 가설: 상세 진입 없는 빠른 액션은 관리 화면 체류 시간을 줄이고 완료율을 올린다.
- 대상: 번호관리 월 3회 이상 사용자
- 성공 기준: 작업 완료까지 평균 탭 수 -20%

## EXP-05 스플래시 모션 최적화
- 가설: 콜드/웜 분리형 스플래시는 초기 이탈률을 낮춘다.
- 대상: 신규 설치 후 첫 3회 실행 사용자
- 성공 기준: 첫 실행 10초 이내 이탈률 -8% 이상

## EXP-06 상호작용 피드백 강화
- 가설: 버튼/볼/시트 피드백 모션은 작업 완료율을 높인다.
- 대상: 번호생성/번호관리 활성 사용자
- 성공 기준: 저장 완료율 +6% 이상, 오류 재시도율 감소

## 5. 측정/운영 규칙
- 각 실험은 최소 2주 유지
- 실패 실험도 `11-progress-tracker.md`에 원인 기록
- 성공 실험만 기본 기능으로 승격

## 6. 문서 연계
- 아이디어 소스: `21-product-enhancement-ideas.md`
- 우선순위: `22-prioritization-matrix.md`
- 실행 작업: `10-detailed-todo-board.md`

## 7. L-태스크 완료 매핑
| ID | 결정/산출 | 반영 위치 |
|---|---|---|
| L-001 | 미확인 결과 배지 UX 목적/문구 방향 확정 | 4장 EXP-01 |
| L-002 | EXP-01 실험/이벤트 기준 확정 | 4장 EXP-01 |
| L-003 | 주간 리포트 카드 IA/가설 확정 | 4장 EXP-02 |
| L-004 | EXP-02 성공 기준 확정 | 4장 EXP-02 |
| L-005 | 번호관리 빠른 액션 후보 정의 | 4장 EXP-04, `21-product-enhancement-ideas.md` A02 |
| L-006 | 빠른 액션 baseline 지표(탭 수) 정의 | 4장 EXP-04 성공 기준 |
| L-007 | 출처별 성과 비교 지표 정의 | 2장 KPI, `22-prioritization-matrix.md` C03 |
| L-008 | ROI 트렌드 요구사항 정의 | 2장 KPI, `22-prioritization-matrix.md` C04 |
| L-009 | 조합 중복도 경고 스펙 방향 정의 | `21-product-enhancement-ideas.md` C01 |
| L-010 | API 지연/실패율 관측 지표 정의 | 2장 KPI, `22-prioritization-matrix.md` E01 |
| L-011 | 로컬 데이터 무결성 점검 규칙 정의 | `21-product-enhancement-ideas.md` E02 |
| L-012 | 릴리즈 위험 점수 산식 방향 정의 | `21-product-enhancement-ideas.md` E04 |
| L-013 | KPI 대시보드 템플릿(핵심 KPI) 확정 | 2장 KPI |
| L-014 | 실험 운영 주기(최소 2주) 확정 | 5장 측정/운영 규칙 |
| L-015 | 실패 실험 종료/기록 규칙 확정 | 5장 측정/운영 규칙 |

## 8. EXP-05/06 이벤트 훅 점검 결과 (2026-02-26 Cycle-07)
| 이벤트 | 현재 연결 상태 | 누락 포인트 | 우선 반영 위치 |
|---|---|---|---|
| `motion_splash_shown` | 연결 | 없음(action enum 고정 완료) | SplashGate |
| `motion_splash_skip` | 연결 | 없음(action enum 고정 완료) | SplashGate |
| `interaction_cta_press` | 연결 | 없음(action enum 고정 완료) | Home/Generator/Manage/Result 공통 CTA |
| `interaction_ball_lock_toggle` | 연결 | 없음(action enum 고정 완료) | Number Generator 잠금 토글 핸들러 |
| `interaction_sheet_apply` | 연결 | 없음(action enum 고정 완료) | Manage 필터/정렬 시트, Result 회차 변경 시트 |

- 코드 기준 확인 결과: `AnalyticsLogger`/`LogcatAnalyticsLogger`가 도입되었고, DI(`AppGraph`)를 통해 주요 화면에서 `interaction_*` 로그를 수집 중이다.
- 스플래시 PR 게이트 적용: `scripts/check-splash-motion-gate.sh`가 splash 소스 감지 시 `motion_splash_shown`/`motion_splash_skip` 누락을 실패 처리한다.
- Cycle-07 반영:
  1. `AnalyticsActionValue` 도입으로 `action` 파라미터 값을 상수화
  2. Home/Generator/Manage/Result/Splash에서 `action` 문자열 리터럴 제거
  3. 대시보드 집계 시 action value drift 리스크 축소

## 9. EXP-05/06 샘플 검증 자동화 (2026-02-26 Cycle-09)
- 검증 스크립트 추가: `scripts/verify-analytics-events.sh`
- 검증 대상:
  - `motion_splash_shown`
  - `motion_splash_skip`
  - `interaction_cta_press`
  - `interaction_ball_lock_toggle`
  - `interaction_sheet_apply`
- 스키마 검증: `interaction_*` 이벤트 로그에 `screen/component/action` 포함 여부 확인
- 실행 방식:
  - 실기/에뮬레이터 연결 시: `adb logcat -d -s WeeklyLottoAnalytics:I` 기반 검사
  - 샘플 파일 검증 시: `--log-file <path>` 옵션 사용
- Cycle-09 결과:
  - 샘플 로그 기반 실행에서 필수 이벤트 5종 + 스키마 검증 PASS
