# 상태 기반 UI 내러티브/마이크로카피 완성도 게이트 스펙 (S23)

## 1. 목적
- "기본 화면은 괜찮지만 상태 화면이 밋밋하거나 불안하다"는 피드백을 릴리즈 전에 차단한다.
- 로딩/빈 상태/오류/오프라인/외부 이동 상태에서 시각 완성도와 신뢰 UX를 일관되게 유지한다.

## 2. 적용 범위
- 핵심 화면: `Home`, `Result`, `Generator`, `Manage`, `Settings`
- 상태 유형: `loading`, `empty`, `error`, `offline`, `redirect`
- 외부 이동 흐름: 공식 구매 링크 안내, 실패 fallback(링크 복사/브라우저 열기)

## 3. S23 Scorecard (N1~N5)
### N1. 첫 7초 인상 연속성
- 확인 항목
  - Splash -> Home 전환에서 톤/타이포/강조 요소가 단절되지 않는다.
  - 첫 진입 시 사용자 시선이 핵심 CTA와 핵심 숫자에 자연스럽게 도달한다.
- PASS 기준
  - 첫 7초 인상 점검에서 단절 이슈 0건

### N2. 상태 화면 시각 일관성
- 확인 항목
  - loading/empty/error/offline/redirect 상태의 카드/타이포/버튼 구조가 공통 규칙을 따른다.
  - 상태별 색/아이콘/강조 방식이 의미와 일치한다.
- PASS 기준
  - 상태별 컴포넌트 규칙 위반 0건

### N3. 마이크로카피 톤/명확성
- 확인 항목
  - 문구가 짧고 행동 지향적이며 책임 회피형 표현이 없다.
  - 오류 문구는 원인 + 다음 행동(재시도/대안)을 함께 제공한다.
- PASS 기준
  - 이해 모호 코멘트 0건
  - 행동 유도 누락 0건

### N4. 모션 리듬/반응성
- 확인 항목
  - 상태 전환 모션이 과하거나 느리지 않고 맥락을 보조한다.
  - Reduce Motion ON에서 의미는 유지되고 과한 전환은 제거된다.
- PASS 기준
  - 상태 전환 지연 체감 이슈 0건
  - Reduce Motion 회귀 0건

### N5. 외부 이동 신뢰 UX
- 확인 항목
  - 외부 이동 전 안내(목적지/시간 제한/성인 인증)가 명확하다.
  - 실패 시 fallback 액션이 2탭 이내 도달 가능하다.
- PASS 기준
  - 외부 이동 실패 복구 성공률 95% 이상
  - fallback 도달 탭 수 2 이하

## 4. 개선 우선순위
1. `error/offline` 상태: 행동 가능한 복구 경로와 문구 선명화(N3, N5)
2. `loading/empty` 상태: 시각 리듬 정렬과 CTA 우선순위 고정(N1, N2)
3. `redirect` 상태: 안내 카드 + fallback 액션 신뢰 UX 고정(N5)
4. 상태 전환 모션/Reduce Motion 동등성 검증(N4)

## 5. 예외처리/안정성/성능 연동
- 네트워크 단절/지연
  - 상태 전환 시 사용자에게 현재 상황과 다음 행동을 즉시 제시한다.
- 외부 앱 미설치/실패
  - fallback 공통 컴포넌트로 대체 경로를 일관 제공한다.
- 저사양 장치
  - 상태 전환 애니메이션을 단계 축소해 프레임 저하를 억제한다.

## 6. 테스트/릴리즈 연동 규칙
- 테스트 계획: `06-test-plan.md`의 `S23` 항목으로 검증
- 릴리즈 체크리스트: `07-release-checklist.md`의 `S23-1~S23-4` 필수 확인
- 우선순위/실험 연동: `22`의 `S23`, `23`의 `EXP-27`과 동일 ID 사용

## 7. 증적 팩(Evidence Pack)
- 필수 산출물
  - 상태 매트릭스 캡처: `loading/empty/error/offline/redirect` x 핵심 화면
  - 1.0x/1.3x/저조도 조건별 캡처 세트
  - 외부 이동 실패 fallback 재현 로그 1건 이상
  - `S23` scorecard 최종 판정표(PASS/WARN/FAIL)
- 파일 규칙
  - `docs/assets/visual-proof-matrix/` + `docs/assets/distribution/` 하위에 날짜 태그로 저장
  - 판정 리포트는 `s23_stateful_ui_narrative_gate_<yyyy-mm-dd>.md` 형식 사용

## 8. 완료 기준(DoD)
- N1~N5 모두 PASS
- `06`/`07`/`10`/`11`/`25` 문서의 `S23` 상태가 동일
- 릴리즈 회의에서 상태 화면 품질 부족 사유로 보류되는 사례 0건

## 9. 문서 연계
- 시각/타이포 기준: `26-visual-typography-refresh.md`
- 모션 기준: `24-motion-and-interaction-playbook.md`
- UI 미감 게이트 기준: `51-visual-aesthetic-and-typography-hardening-spec.md`
