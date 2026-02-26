# 디자인 매핑 (SVG → 화면/컴포넌트)

## 0. 추가 레퍼런스(Figma)
- URL: `https://www.figma.com/design/DY43CuXVwQwlqakFfR2yM1/Home?node-id=6-2&t=gyLKDBrGebdRNSZI-1`
- 대상 노드: `6:2`
- 현재 상태: Figma MCP 호출 한도 이슈로 노드 메타데이터/스크린샷 직접 수집은 보류
- 최근 확인: 2026-02-26 재시도 시 동일 한도 응답(플랜 업그레이드 안내)
- 대응: 저장소 `design/*.svg`를 기준 구현 완료 후, Figma 접근 가능 시 간격/타이포/밀도 차이를 최종 보정
- 오프라인 대응: `docs/19-offline-design-qa-checklist.md` 기준으로 스크린샷 대조 QA 수행

## 0-1. 추가 레퍼런스(Compose 스펙 문서)
- 파일: `/Volumes/무제/design_spec_android_lotto.md`
- 적용 범위:
  - 디자인 토큰(색/간격/타이포) 일부 반영
  - 공통 컴포넌트(`LottoTopAppBar`, `LottoBottomBar`, `BallChip`, `TicketCard`, `StatusBadge`) 반영
  - `Manage` 탭 골격(편집모드/필터시트/FAB시트/삭제다이얼로그) 반영
  - `ManualAdd`, `Import`, `TicketDetail` 서브 라우트 반영
  - `Result` 회차 피커의 실제 회차 데이터 연동(`fetchByRound`) 반영
  - `QR 스캔 완료 -> 저장 확인 시트(취소/저장)` 플로우 반영
  - Home/Manage/Result/Generator의 간격·타이포·카드 라운드 픽셀 보정(J-007) 반영
- 잔여 항목:
  - Figma MCP 호출 한도 해소 후 node `6:2` 직접 대조(최종 시각 QA)

## 1. 홈 (`design/lotto_home.svg`)
- 헤더: 앱 타이틀 + 설정 진입
- 상단 카드: 회차, D-day
- CTA 2개: QR 스캔, 번호 생성
- 구매 번호 리스트: 게임별 번호볼 표시
- 하단 탭: 홈/번호관리/당첨결과(+통계 확장)

## 2. 번호생성 (`design/lotto_generator.svg`)
- 헤더: 제목 + 전체 초기화
- 안내문: 잠금 동작 가이드
- A~E 게임 카드
- 번호볼 탭으로 잠금 토글
- 하단 액션:
  - 잠금 제외 랜덤 재생성
  - 이번 주 번호 저장

## 3. 당첨결과 (`design/lotto_result.svg`)
- 회차 + 추첨일 + 당첨번호 6 + 보너스
- 내 구매번호 리스트
- 게임별 배지(5등/낙첨 등)
- 일치 번호 하이라이트

## 4. 위젯 (`design/lotto_widgets.svg`)
### 타입 A (4x2)
- 이번주 회차/디데이
- A,B 게임 우선 노출
- QR 스캔 진입 액션
- 나머지 게임 수 요약

### 타입 B (4x1)
- 최신 회차 결과 라벨
- 당첨번호 6 + 보너스
- 내 결과 요약 문구

## 5. 컬러 룰
- 1~10 노랑, 11~20 파랑, 21~30 빨강, 31~40 초록, 41~45 회색
- 당첨 화면에서 불일치 번호는 회색 outline 처리

## 6. 실물 복권 스캔 참고 포인트
- QR 위치: 대체로 우측 상단 영역
- 텍스트 위치: 중앙 회차/일시, 하단 게임 번호(A~E)
- 다중 용지 촬영: 한 프레임에 여러 장이 들어오면 인식 충돌 가능성이 높아 1장씩 촬영 권장
- 앱 가이드 반영: QR 스캔 화면에서 우측 상단 QR 정렬 안내 문구를 기본 노출
