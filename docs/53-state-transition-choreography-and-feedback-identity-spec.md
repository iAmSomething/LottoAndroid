# 상태 전환 코레오그래피/피드백 아이덴티티 게이트 스펙 (S24)

## 1. 목적
- 화면은 예쁜데 전환 체감이 어색하거나 반응이 들쭉날쭉한 문제를 릴리즈 전에 차단한다.
- 상태 전환(`enter/steady/exit`)과 피드백 계층(`visual/haptic/copy`)을 하나의 품질 게이트로 고정한다.

## 2. 적용 범위
- 핵심 화면: `Home`, `Result`, `Generator`, `Manage`, `Settings`
- 전환 단위: 화면 진입/이탈, 시트 열림/닫힘, 리스트 갱신, 외부 이동/복귀
- 피드백 계층: 시각 반응, 햅틱 반응, 마이크로카피 반응

## 3. S24 Scorecard (C1~C5)
### C1. 전환 연속성
- 확인 항목
  - `enter -> steady -> exit` 흐름이 단절 없이 이어진다.
  - 전환 직후 레이아웃 흔들림/깜빡임이 없다.
- PASS 기준
  - 전환 단절 이슈 0건

### C2. 피드백 계층 일치성
- 확인 항목
  - 같은 중요도의 액션은 visual/haptic/copy 강도가 일관된다.
  - 성공/주의/실패 상태의 피드백 의미가 충돌하지 않는다.
- PASS 기준
  - 계층 불일치 이슈 0건

### C3. 속도/완급 균형
- 확인 항목
  - 빠른 액션은 즉시 반응하고, 정보성 전환은 읽을 수 있는 속도를 유지한다.
  - 과한 모션으로 인한 지연 체감이 없다.
- PASS 기준
  - 반응 지연 체감 이슈 0건

### C4. 접근성 동등성
- 확인 항목
  - Reduce Motion ON에서도 상태 의미가 동일하게 전달된다.
  - 1.3x 폰트스케일에서 전환 중 잘림/겹침이 없다.
- PASS 기준
  - 접근성 회귀 0건

### C5. 오류 복구 명확성
- 확인 항목
  - 실패 전환(네트워크 오류/외부 이동 실패) 시 다음 행동이 즉시 제시된다.
  - fallback 경로(재시도/복사/브라우저 열기)가 2탭 이내로 도달된다.
- PASS 기준
  - 복구 동선 혼선 0건
  - fallback 도달 탭 수 2 이하

## 4. 개선 우선순위
1. 실패 전환 복구 구간(error/offline/redirect) 정렬(C5)
2. 시트/리스트/탭 전환 속도/완급 재정렬(C1, C3)
3. 액션 피드백 강도 매핑 표준화(C2)
4. Reduce Motion/폰트스케일 접근성 동등성 고정(C4)

## 5. 예외처리/안정성/성능 연동
- 저사양 장치
  - 전환 효과를 단계 축소해 프레임 저하를 억제한다.
- 외부 이동 실패
  - fallback 공통 컴포넌트로 복구 경로를 즉시 제공한다.
- 네트워크 지연/실패
  - 상태 전환과 동시에 행동 가능한 안내를 함께 노출한다.

## 6. 테스트/릴리즈 연동 규칙
- 테스트 계획: `06-test-plan.md`의 `S24` 항목으로 검증
- 릴리즈 체크리스트: `07-release-checklist.md`의 `S24-1~S24-4` 필수 확인
- 우선순위/실험 연동: `22`의 `S24`, `23`의 `EXP-28`과 동일 ID 사용

## 7. 증적 팩(Evidence Pack)
- 필수 산출물
  - `enter/steady/exit` 전환 캡처 또는 녹화(핵심 화면 4종)
  - feedback layer(visual/haptic/copy) 매핑 표 + 재현 로그
  - Reduce Motion ON + 1.3x 폰트스케일 검증 캡처
  - `S24` scorecard 최종 판정표(PASS/WARN/FAIL)
- 파일 규칙
  - `docs/assets/visual-proof-matrix/` + `docs/assets/distribution/` 하위에 날짜 태그로 저장
  - 판정 리포트는 `s24_transition_feedback_gate_<yyyy-mm-dd>.md` 형식 사용

## 8. 완료 기준(DoD)
- C1~C5 모두 PASS
- `06`/`07`/`10`/`11`/`25` 문서의 `S24` 상태가 동일
- 릴리즈 회의에서 전환 품질/반응 불일치 사유로 보류되는 사례 0건

## 9. 문서 연계
- 모션 기본 기준: `24-motion-and-interaction-playbook.md`
- UI 미감 기준: `51-visual-aesthetic-and-typography-hardening-spec.md`
- 상태 내러티브/카피 기준: `52-stateful-ui-narrative-and-microcopy-hardening-spec.md`
