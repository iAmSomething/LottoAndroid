# 비주얼/타이포그래피 리프레시 가이드 (v1)

## 1. 목적
- 앱이 "기능은 되는데 예쁘지 않다"는 인상을 벗어나도록 시각 언어를 재정의한다.
- 특히 타이포를 기본 시스템 느낌에서 브랜드 톤이 있는 스타일로 전환한다.

## 2. 현재 진단
- `Type.kt`는 현재 브랜드 폰트 리소스(`brand_noto_sans_kr_variable`, `brand_roboto_condensed_variable`, `brand_roboto_mono_variable`)를 직접 사용한다.
- 제목/본문/숫자 위계는 1차 개선됐으나, 폰트 자체 개성이 약해 "기본 앱 느낌"이 남는다.
- 컬러 팔레트는 1차 전환됐지만 배경 질감/카드 레이어/아이콘 톤이 아직 기능 중심이라 감성 밀도가 낮다.

## 3. 리프레시 방향: "Lucky Editorial"
- 키워드: 선명함, 신뢰감, 숫자 중심 가독성, 과하지 않은 축제감
- 원칙:
  - 숫자/회차/당첨금은 강한 대비로 "한 번에 보이게"
  - 본문은 안정적인 가독성 우선
  - CTA는 화면마다 1개만 강하게 강조

## 4. 폰트 시스템(제안)
## 4.1 폰트 페어
- Display/Headline: `Gmarket Sans` (Bold/Medium)
- Body/UI: `Pretendard` (Regular/Medium/SemiBold)
- Numeric 강조: `Roboto Condensed` (Bold) + fallback `Pretendard`

## 4.2 타입 스케일(M3 매핑 기준)
- `headlineLarge` (회차/핵심 숫자): 30/36, Bold, Gmarket Sans
- `headlineMedium` (섹션 핵심): 24/30, Bold, Gmarket Sans
- `titleLarge` (카드 타이틀): 20/26, SemiBold, Pretendard
- `titleMedium` (리스트 타이틀): 17/24, SemiBold, Pretendard
- `bodyLarge` (주요 본문): 16/24, Regular, Pretendard
- `bodyMedium` (기본 본문): 14/21, Regular, Pretendard
- `labelLarge` (CTA): 14/20, SemiBold, Pretendard
- `labelSmall` (메타/캡션): 11/16, Medium, Pretendard

## 4.3 숫자 표기 규칙
- 회차/당첨금/합계/카운트는 `Roboto Condensed Bold` 우선
- 숫자와 단위는 자간을 분리해 가독성 확보(예: `1,234,000 원`)

## 5. 컬러 톤 리프레시(제안)
- Primary: Deep Teal `#0F5B63`
- Primary Strong: `#0A3F45`
- Accent: Warm Gold `#E0B34A`
- Background: Warm Ivory `#F7F4EE`
- Surface: `#FFFCF7`
- Text Primary: `#1C2228`
- Text Secondary: `#4A5561`
- Border: `#DDE3E8`

현재 Purple 계열은 전면 교체가 아니라 "점진 치환"을 원칙으로 한다.

## 6. 화면별 1차 적용 우선순위
1. Home: 헤더/회차 카드/주간 리포트 카드 타이포 위계 재설계
2. Result: 당첨번호/합계 당첨금 숫자 강조형 레이아웃
3. Number Generator: A~E 게임 타이틀/상태 라벨 대비 강화
4. Manage: 리스트 메타(회차/등록일/상태) 밀도 재조정
5. Wear: 원형 화면 기준 숫자/짧은 라벨 우선 타이포 적용

## 7. 구현 가이드(코드 착수 전)
- `ui/theme/Type.kt`:
  - 폰트 리소스 연결(`FontFamily`) 추가
  - 타입 스케일을 `headline/title/body/label` 체계로 재구성
- `ui/theme/Color.kt`:
  - 톤 교체 시 토큰 호환 레이어를 두고 점진 전환
- 컴포넌트:
  - `LottoTopAppBar`, `TicketCard`, `LottoBottomBar`, `BallChip`부터 우선 반영

## 8. QA 체크리스트
- 제목/본문/메타가 3단계 위계로 즉시 구분되는가
- 회차/당첨금 숫자가 스크롤 없이 첫 시선에 들어오는가
- 저조도/실외 밝은 화면에서 대비가 유지되는가
- 접근성 폰트 스케일 1.3x에서 레이아웃 깨짐이 없는가

## 9. 작업 보드 매핑
- `10-detailed-todo-board.md`의 `R-001 ~ R-012`로 추적한다.

## 10. Cycle-04 구현 반영 (2026-02-26)
- 코드 반영:
  - `ui/theme/Color.kt`: 딥틸/웜골드/아이보리 팔레트 1차 적용
  - `ui/theme/Type.kt`: `FontFamily` 지정 + `LottoTypeTokens.Numeric*` 정의
  - `ui/component/LottoTopAppBar.kt`, `TicketCard.kt`, `LottoBottomBar.kt`, `LottoBall.kt` 타이포/위계 정렬
  - `feature/home/HomeScreen.kt`, `feature/result/ResultScreen.kt` 숫자 강조 스타일 적용
- 스크린샷 증적:
  - Home 전/후: `docs/assets/typography-refresh/home_before.png`, `home_after.png`
  - Result 전/후: `docs/assets/typography-refresh/result_before.png`, `result_after.png`
  - 접근성 1.3x: `home_font_1_3x.png`, `result_font_1_3x.png`

## 11. Wear 타이포 밀도 가이드(원형 UI)
- 우선순위:
  - 1순위 숫자(회차/당첨금/카운트): 18~20sp, Bold, 한 줄 고정
  - 2순위 상태 라벨(당첨/미확인): 12~13sp, SemiBold
  - 3순위 보조 설명: 10~11sp, Medium
- 레이아웃 규칙:
  - 원형 안전 영역 중심(상하 8dp, 좌우 10dp) 내 3단 구조 유지
  - 한 카드에서 텍스트 줄 수 최대 4줄(숫자 1줄 + 상태 1줄 + 메타 2줄)
  - CTA 라벨은 2단어 이내(예: `결과 보기`, `폰에서 열기`)
- 가독성 규칙:
  - 배경과 텍스트 대비 4.5:1 이상
  - 1.2x 폰트 스케일에서 줄바꿈 허용, 1.3x 이상은 메타 우선 생략

## 12. Cycle-14 폴리시 확장안 (2026-02-26)
- 목표:
  - "타이포는 적용됐는데 아직 안 예쁨" 문제를 해결하기 위한 2차 시각 완성도 개선
- 핵심 과제:
  1. 실제 브랜드 폰트 리소스 연동(`res/font`) 및 fallback 체계 확정
  2. 숫자 강조 토큰의 문맥 규칙 분리(회차/당첨금/카운트)
  3. 카드 레이어 체계(배경/표면/강조면)와 그림자 규칙 정규화
  4. 상단 영역/핵심 카드에 미세 질감(그라디언트/노이즈) 적용 가이드 작성
  5. 아이콘 스트로크/라운드 규칙 통일(혼합 스타일 제거)

## 13. Cycle-15 과거 상태 점검 (2026-02-26)
- 당시 코드 확인 결과:
  - `LottoDisplayFontFamily = SansSerif`
  - `LottoBodyFontFamily = SansSerif`
  - `LottoNumericFontFamily = Monospace`
- 당시 해석:
  - 타입 스케일은 정리됐지만, 실제 브랜드 폰트 미적용으로 비주얼 임팩트가 제한됨
- 당시 즉시 우선순위:
  1. `AB-005`: `res/font` 자산 도입
  2. `AB-006`: `Type.kt`를 실폰트 매핑으로 전환

## 14. Cycle-42 최신 상태 (2026-02-26)
- 현재 코드 확인 결과:
  - `LottoDisplayFontFamily`: `brand_roboto_condensed_variable`
  - `LottoBodyFontFamily`: `brand_noto_sans_kr_variable`
  - `LottoNumericFontFamily`: `brand_roboto_mono_variable`
- 해석:
  - fallback 단계는 종료되었고, 실폰트 적용 이후 미세 타이포/가독성 최적화 단계로 전환됨

## 15. Cycle-53 UI 품질 게이트 연동
- `S11-U1/U2` 기준에 따라 타이포/비주얼 판정은 캡처 단건이 아니라 1.0x/1.3x 세트로 수행한다.
- 필수 확인:
  - 숫자+단위 줄바꿈 분리 금지
  - 화면당 핵심 CTA 1개 강조 원칙 유지
  - Home/Result/Manage의 타이포 위계(헤드라인/본문/메타) 유지

## 16. Cycle-54 통합 결론 연동
- 타이포/비주얼 결과(`S11-U1/U2`)는 `41-unified-quality-verdict-package-spec.md`의 통합 결론 입력으로 사용한다.

## 17. Cycle-55 드라이런/에스컬레이션 연동
- 타이포/비주얼 보류 케이스는 `42-unified-verdict-dryrun-and-escalation-spec.md`의 E13-2 규칙으로 즉시 이관한다.

## 18. Cycle-56 이력/추세 연동
- 타이포/비주얼 회귀 이력은 `43-unified-verdict-history-and-trend-spec.md`에 따라 주간 추세로 관리한다.

## 19. Cycle-57 위험예산/프리즈 연동
- 타이포/비주얼 관련 반복 보류는 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 코드별 한도 규칙으로 관리한다.

## 20. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- 타이포/비주얼 프리즈 이슈의 상태 공유/해제 판단은 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 21. Cycle-59 프리즈 드릴/준비도 연동
- 타이포/비주얼 프리즈 대응은 `46-freeze-drill-readiness-score-spec.md` 드릴 시나리오로 검증한다.

## 22. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- 타이포/비주얼 프리즈 이슈 후속 액션의 폐쇄 게이트/재개방 기준은 `47-freeze-drill-corrective-action-loop-spec.md`를 따른다.

## 23. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- 타이포/비주얼 프리즈 이슈의 debt 점수 및 차단/해제 정책은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
