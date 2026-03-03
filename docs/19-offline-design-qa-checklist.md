# 오프라인 디자인 QA 체크리스트 (Figma MCP 제한 대응)

## 1. 목적
- Figma MCP 호출 제한으로 node `6:2` 직접 대조가 불가능한 기간에도 UI 정합성 QA를 지속한다.
- 기준 문서:
  - `/Volumes/무제/design_spec_android_lotto.md`
  - `docs/08-design-mapping.md`
  - `docs/assets/store-screenshots/*.png`

## 2. 실행 절차
1. 앱 최신 빌드 설치 후 주요 화면(Home/Manage/Generator/Result/QR/Stats) 캡처
2. `docs/assets/store-screenshots/*.png`와 현재 캡처를 1:1 비교
3. 아래 체크리스트 항목별 Pass/Fail 기록
4. Fail 항목은 `docs/10-detailed-todo-board.md`에 태스크 추가
5. 결과를 `docs/11-progress-tracker.md`에 날짜와 함께 기록

## 3. 토큰 정합성 체크
- [x] 상단 앱바 그라데이션: `Primary(#0F5B63)` ~ `PrimaryDark(#0A3F45)`
- [x] 기본 배경: `Background(#F7F4EE)`
- [x] 기본 카드 반경: `18dp`
- [x] 시트 반경: `22dp`
- [x] 화면 기본 좌우 패딩: `16dp`
- [x] 번호볼 크기: 기본 `24dp`, 강조 `28dp`

## 4. 컴포넌트 체크
- [x] `LottoTopAppBar`: 제목/우측 액션 정렬, 높이 96dp 근접
- [x] `LottoBottomBar`: 3탭(Home/번호관리/당첨결과) 활성색/비활성색 구분
- [x] `BallChip`: 1~45 컬러 룰(노/파/빨/초/회) 유지
- [x] `TicketCard`: 제목/번호볼/메타 텍스트 밀도 일관성
- [x] `StatusBadge`: 상태별 톤 구분(대기/당첨/낙첨)

## 5. 화면별 체크
### Home
- [x] 회차 카드, CTA 2개(QR/번호생성), 이번 주 구매번호 섹션 구조 유지

### Generator
- [x] A~E 게임 카드 구조 유지
- [x] 잠금/재생성/저장 CTA 위치 및 우선순위 유지

### Manage
- [x] 이번주/보관함/스캔내역 탭 구조 유지
- [x] 편집모드/삭제/필터 동선 정상

### Result
- [x] 당첨번호 + 보너스 번호 레이아웃 유지
- [x] 상태 배지 및 회차 전환 액션 유지

### QR
- [x] 오버레이 가이드, 수동 입력 백업, 실패 가이드 동선 유지

### Stats
- [x] 기간 필터(전체/4주/8주), 누적 지표, TOP 번호 구조 유지

## 6. 기록 템플릿
```text
[YYYY-MM-DD]
- 디바이스: (예: Pixel 8, Android 15)
- 비교 기준: docs/assets/store-screenshots/*
- Pass:
  - ...
- Fail:
  - ...
- 조치:
  - TODO 보드 항목 ID
```

## 7. 현재 상태 (2026-02-26)
- 오프라인 QA 프로세스 문서화 완료
- 오프라인 QA 체크리스트 Pass(2026-02-26, 에뮬레이터 캡처 기준)
  - 증적: `docs/assets/visual-proof-matrix/*.png`
  - 보조 증적: `docs/assets/store-screenshots/*.png`
- Figma MCP 직접 대조는 호출 한도 해소 시 재개 예정
