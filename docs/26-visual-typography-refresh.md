# 비주얼/타이포그래피 리프레시 가이드 (v1)

## 1. 목적
- 앱이 "기능은 되는데 예쁘지 않다"는 인상을 벗어나도록 시각 언어를 재정의한다.
- 특히 타이포를 기본 시스템 느낌에서 브랜드 톤이 있는 스타일로 전환한다.

## 2. 현재 진단
- `Type.kt`는 `fontFamily` 지정이 없어 시스템 기본 폰트 렌더링에 의존한다.
- 제목/본문/숫자의 역할 분리가 약해 정보 위계가 평평하게 보인다.
- 보라 중심 컬러가 강해 화면 인상이 단조롭고, 카드/CTA 대비가 약하다.

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
