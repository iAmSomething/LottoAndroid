# 지각 지연/로딩 연속성 게이트 스펙 (S31)

## 1. 목적
- 실제 성능 수치가 같아도 "느리다"고 느끼는 UX 공백을 릴리즈 전에 차단한다.
- 로딩 중 단절감, 깜빡임, 상태 혼선을 줄여 사용자의 작업 연속성을 유지한다.

## 2. 적용 범위
- 대상 화면: `Home`, `Result`, `Generator`, `Manage`, `Settings`
- 대상 상태: cold/warm 진입, API 로딩, 리스트 갱신, 외부 이동 복귀 직후
- 대상 요소: skeleton/placeholder, progress indicator, cached fallback, retry CTA

## 3. S31 Scorecard (P1~P5)
### P1. 즉시 반응 인지성
- 확인 항목
  - 사용자 액션 후 100ms 이내에 시각/촉각 반응이 시작된다.
  - 네트워크 대기 중에도 입력이 수신됐다는 신호가 명확하다.
- PASS 기준
  - 무반응 체감 이슈 0건

### P2. 로딩 연속성/레이아웃 안정성
- 확인 항목
  - skeleton/placeholder가 최종 레이아웃과 의미적으로 연결된다.
  - 로딩 완료 시 레이아웃 점프/깜빡임이 최소화된다.
- PASS 기준
  - 로딩 전후 레이아웃 불연속 이슈 0건

### P3. 진행 상태/타임아웃 명확성
- 확인 항목
  - 진행 상태가 단계적으로 표현되고 장시간 대기 시 원인/다음 행동이 제시된다.
  - timeout 시 재시도/대체 경로가 즉시 노출된다.
- PASS 기준
  - 대기 상태 혼선/방치 0건

### P4. 캐시 복원/재시도 완결성
- 확인 항목
  - 실패 시 최근 유효 데이터 fallback이 동작하고 stale 정보임을 구분해 표시한다.
  - 재시도 성공 후 상태가 자연스럽게 최신 값으로 전환된다.
- PASS 기준
  - fallback 부재 또는 복원 실패 0건

### P5. 접근성/저사양 동등성
- 확인 항목
  - 스크린리더/폰트스케일 1.3x/Reduce Motion에서도 로딩 의미가 유지된다.
  - 저사양 장치에서 로딩 전환 품질이 임계치 이내로 유지된다.
- PASS 기준
  - 접근성/저사양 조건별 로딩 회귀 0건

## 4. 개선 우선순위
1. 입력 후 즉시 반응 신호 고정(P1)
2. skeleton-최종 레이아웃 연결성 보장(P2)
3. 장시간 대기/timeout 메시지 및 재시도 경로 표준화(P3)
4. 캐시 fallback 및 복원 전환 규칙 고정(P4)
5. 접근성/저사양 동등성 검증(P5)

## 5. 예외처리/안정성/성능 연동
- 네트워크 지연/불안정
  - 단계형 진행 표시 + timeout 후 복구 CTA를 2탭 이내로 제공한다.
- 데이터 파싱 실패
  - 이전 성공 스냅샷 fallback + 오류 원인 요약 + 재시도 경로를 즉시 노출한다.
- 저사양 장치
  - placeholder 애니메이션 강도를 축소하고 의미 전달을 우선한다.

## 6. 테스트/릴리즈 연동 규칙
- 테스트 계획: `06-test-plan.md`의 `S31` 항목으로 검증
- 릴리즈 체크리스트: `07-release-checklist.md`의 `S31-1~S31-4` 필수 확인
- 우선순위/실험 연동: `22`의 `S31`, `23`의 `EXP-35`와 동일 ID 사용

## 7. 증적 팩(Evidence Pack)
- 필수 산출물
  - 로딩 전/중/후 캡처(핵심 4화면)
  - timeout 및 retry/fallback 복구 로그(최소 1건)
  - cache stale -> fresh 전환 로그(최소 1건)
  - `S31` scorecard 최종 판정표(PASS/WARN/FAIL)
- 파일 규칙
  - `docs/assets/visual-proof-matrix/` + `docs/assets/distribution/` 하위에 날짜 태그로 저장
  - 판정 리포트는 `s31_perceived_latency_loading_continuity_gate_<yyyy-mm-dd>.md` 형식 사용

## 8. 완료 기준(DoD)
- P1~P5 모두 PASS
- `06`/`07`/`10`/`11`/`25` 문서의 `S31` 상태가 동일
- 릴리즈 회의에서 로딩 단절/무반응 체감 사유 보류 0건

## 9. 문서 연계
- 성능 캘리브레이션 기준: `37-performance-gate-calibration-spec.md`
- 성능 실행/판정 기준: `38-performance-gate-execution-template.md`
- 상호작용 확정 상태 기준: `57-interaction-trust-signal-and-confirmed-state-gate-spec.md`
