# 디자인 매핑 (SVG → 화면/컴포넌트)

## 0. 추가 레퍼런스(Figma)
- URL: `https://www.figma.com/design/DY43CuXVwQwlqakFfR2yM1/Home?node-id=6-2&t=gyLKDBrGebdRNSZI-1`
- 대상 노드: `6:2`
- 현재 상태: Figma MCP 호출 한도 이슈로 노드 메타데이터/스크린샷 직접 수집은 보류
- 최근 확인: 2026-02-26 재시도 시 동일 한도 응답(플랜 업그레이드 안내)
- 점검 로그: `docs/assets/figma/mcp_limit_check_2026-02-26.md`
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
- 미확인 결과 배지 카드: 최신 결과 미확인 시 강조 노출 + 결과화면 진입
- 주간 리포트 카드: 최신 결과 기준 구매/당첨/순이익 요약
- 구매 번호 리스트: 게임별 번호볼 표시
- 하단 탭: 홈/번호관리/당첨결과(+통계 확장)

## 2. 번호생성 (`design/lotto_generator.svg`)
- 헤더: 제목 + 전체 초기화
- 안내문: 잠금 동작 가이드
- A~E 게임 카드
- 번호볼 탭으로 잠금 토글
- 수동 입력 편집 영역:
  - 빠른 후보(미포함 번호) 칩 제공
  - 1~45 번호 팔레트 직접 선택
  - 선택 번호 반영/입력 초기화 액션
  - 현재 선택 게임 번호/잠금 상태 시각화
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
- 리디자인 반영:
  - 보라 계열 카드 배경 + 라운드 모서리
  - 상단 브랜드/회차 정보와 QR CTA 위계 분리
  - 게임 행을 반투명 서브카드로 구분해 가독성 강화

### 타입 B (4x1)
- 최신 회차 결과 라벨
- 당첨번호 6 + 보너스
- 내 결과 요약 문구
- 리디자인 반영:
  - 화이트 카드 + 강조 요약문구 컬러(당첨/낙첨 상태 분기)
  - 결과 보기 CTA를 칩 버튼화
  - 번호 라인과 보너스 라인을 분리해 정보 밀도 개선

## 5. 컬러 룰
- 1~10 노랑, 11~20 파랑, 21~30 빨강, 31~40 초록, 41~45 회색
- 당첨 화면에서 불일치 번호는 회색 outline 처리

## 6. 실물 복권 스캔 참고 포인트
- QR 위치: 대체로 우측 상단 영역
- 텍스트 위치: 중앙 회차/일시, 하단 게임 번호(A~E)
- 다중 용지 촬영: 한 프레임에 여러 장이 들어오면 인식 충돌 가능성이 높아 1장씩 촬영 권장
- 앱 가이드 반영: QR 스캔 화면에서 우측 상단 QR 정렬 안내 문구를 기본 노출

## 7. 모션/상호작용 매핑
- 스플래시:
  - 콜드 스타트: 브랜드 인트로(짧은 로고 스케일 + 페이드)
  - 웜 스타트: 축약 모션(즉시 홈 진입 우선)
  - 구현 상태: `SplashGate` 적용(콜드 900ms / 웜 300ms)
  - 증적: `docs/assets/typography-refresh/splash_cold.png`, `splash_warm.png`
- 홈:
  - 상단 카드/CTA는 첫 진입 시 짧은 stagger reveal
  - 재진입 시 애니메이션 최소화(반복 피로 방지)
- 번호생성:
  - BallChip 상태 전이(선택/잠금/적중) 모션 통일
  - 수동 입력 반영 시 선택 슬롯 강조 피드백
- 번호관리:
  - 편집 모드 진입/해제 시 action bar 상태 전환 애니메이션
  - 삭제/이동 시 리스트 collapse 전이
- 당첨결과:
  - 당첨/낙첨 배지 상태 전환 피드백
  - 회차 변경 시트 오픈/닫힘 모션 일관성 유지

세부 규격은 `24-motion-and-interaction-playbook.md`를 기준으로 적용한다.

## 8. 비주얼/타이포 리프레시 매핑
- 기준 문서: `26-visual-typography-refresh.md`
- 핵심 적용 포인트:
  - Home/Result의 숫자 정보(회차/당첨금/합계)를 Display/Numeric 스타일로 우선 전환
  - 카드 타이틀/본문/메타를 3단 위계로 분리
  - CTA는 화면당 1개만 강한 대비를 유지
- 구현 우선 컴포넌트:
  - `LottoTopAppBar`, `TicketCard`, `LottoBottomBar`, `BallChip`

## 9. UI 비주얼 폴리시 2차 매핑
- 기준 문서: `27-ui-visual-polish-pack.md`
- 핵심 적용 포인트:
  - Home/Result/Generator/Manage의 카드 레이어와 강조 규칙 통일
  - 아이콘/버튼/배지 스타일 편차 축소
  - 배경 질감/그라디언트는 핵심 영역만 제한적으로 적용
- Cycle-04 증적:
  - Home 전/후: `docs/assets/typography-refresh/home_before.png`, `home_after.png`
  - Result 전/후: `docs/assets/typography-refresh/result_before.png`, `result_after.png`
  - 접근성 1.3x: `home_font_1_3x.png`, `result_font_1_3x.png`

## 10. 구매 리다이렉트 UX 매핑
- 목적:
  - 앱 내에서 동행복권 공식 구매 페이지로 진입 단계를 단축한다.
- 1차 노출 확정(2026-02-26):
  - Home 화면 `공식 홈페이지에서 구매하기` CTA를 1차 진입점으로 고정
  - Result/Settings는 2차 확장 후보로 유지
- 확장 반영(2026-02-26):
  - Settings 화면에도 동일 CTA를 추가해 Home 외 경로에서 fallback 공통 컴포넌트 회귀를 검증
- 상호작용 규칙:
  - 1회 안내 모달 노출(성인 인증/구매 가능 시간/외부 이동 고지)
  - 확인 시 외부 브라우저 또는 Custom Tab으로 `https://dhlottery.co.kr` 이동
  - 실패 시 `링크 복사`와 `기본 브라우저로 열기` 대체 액션 제공
- 이벤트 계측 키(확정):
  - `interaction_cta_press` + `screen=home`, `component=purchase_redirect_cta`, `action=purchase_redirect_tap`
  - `interaction_cta_press` + `screen=home`, `component=purchase_redirect_cta`, `action=purchase_redirect_confirm`
  - `interaction_cta_press` + `screen=home`, `component=purchase_redirect_cta`, `action=purchase_redirect_fail`, `error_type=external_open_failed`
  - `interaction_cta_press` + `screen=home`, `component=purchase_redirect_cta`, `action=purchase_redirect_open_browser`
  - `interaction_cta_press` + `screen=home`, `component=purchase_redirect_cta`, `action=purchase_redirect_copy_link`
- 문구 가이드:
  - "구매는 공식 홈페이지에서만 가능" 문구를 CTA 상단에 고정
  - 연령/시간 제한 문구를 모달에 포함
- 상세 스펙:
  - 오류 매핑/계측/fallback 상세는 `34-exception-mapping-and-redirect-spec.md` 기준으로 적용
  - 사용자 여정 리허설/핫패스 성능 운영은 `35-usecase-reliability-and-performance-playbook.md` 기준으로 적용
  - 구현 슬라이스/게이트/롤백 규칙은 `36-hardening-implementation-slices.md` 기준으로 적용
  - 성능 게이트 캘리브레이션(emulator/device 분리, 반복측정)은 `37-performance-gate-calibration-spec.md` 기준으로 적용
  - 성능 게이트 실행 템플릿/판정 트리는 `38-performance-gate-execution-template.md` 기준으로 적용
  - 성능 게이트 증적 패키지(E1~E5, 최종 결론 템플릿)는 `39-performance-gate-evidence-package-spec.md` 기준으로 적용

## 11. Cycle-53 UI 품질 게이트 연동
- UI 품질 게이트/상호작용 안정성 기준은 `40-ui-quality-gate-and-interaction-resilience-spec.md`를 따른다.
- 핵심 연동:
  - U1: Home/Result/Manage 타이포 위계 및 숫자+단위 규칙
  - U2: 카드/CTA/아이콘 일관성 점검
  - U3/U4: 외부 이동 fallback, Reduce Motion, 1.3x 폰트 스케일 검증

## 12. Cycle-54 통합 결론 연동
- `S10`(성능) + `S11`(UI) 단일 결론 규칙은 `41-unified-quality-verdict-package-spec.md`를 따른다.
- 디자인 매핑 검증 결과는 `S11` 입력물로 제출하고, 최종 릴리즈 결론은 `S12`에서 확정한다.

## 13. Cycle-55 드라이런/에스컬레이션 연동
- `S12` 통합 결론의 드라이런/SLA/에스컬레이션 규칙은 `42-unified-verdict-dryrun-and-escalation-spec.md`를 따른다.

## 14. Cycle-56 이력/추세 연동
- 통합 결론 이력/주간 추세 관리는 `43-unified-verdict-history-and-trend-spec.md`를 따른다.

## 15. Cycle-57 위험예산/프리즈 연동
- 통합 결론 위험예산/프리즈 정책은 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`를 따른다.

## 16. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- 통합 결론 프리즈 지휘/커뮤니케이션 운영은 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 17. Cycle-59 프리즈 드릴/준비도 연동
- 프리즈 드릴/준비도 운영은 `46-freeze-drill-readiness-score-spec.md`를 따른다.

## 18. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- 프리즈 드릴 후속 보정 액션의 등록/폐쇄/재개방 운영은 `47-freeze-drill-corrective-action-loop-spec.md`를 따른다.

## 19. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- 보정 액션 부채 점수 및 릴리즈 차단/해제 정책은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
