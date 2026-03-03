# UI 품질 게이트/상호작용 안정성 스펙 (Cycle-53)

## 1. 목적
- "UI가 덜 예쁘다"는 체감 이슈를 주관 평가가 아닌 운영 가능한 품질 게이트로 고정한다.
- 타이포/컬러/모션/상호작용을 안정성/성능 관점과 함께 판정해 릴리즈 직전 편차를 줄인다.

## 2. UI 품질 게이트 구성(S11)
| 항목 | 설명 | 필수 증적 |
|---|---|---|
| U1 | 타이포 위계 게이트: 헤드라인/본문/숫자 위계, 숫자+단위 표기 규칙 | Home/Result/Manage 1.0x·1.3x 캡처 |
| U2 | 비주얼 일관성 게이트: 카드 레이어/아이콘/CTA 대비 규칙 | 화면 4종 전후 비교 캡처 + 판정표 |
| U3 | 상호작용 응답 게이트: press feedback, 시트/탭 전이, 외부 이동 fallback | 상호작용 로그 + 실패 fallback 캡처 |
| U4 | 접근성/저사양 게이트: Reduce Motion, 큰 글씨, 저사양 graceful degradation | Reduce Motion ON/OFF 캡처 + 성능 요약 |
| U5 | 성능 연동 게이트: UI polish 적용 후 startup/jank 회귀 감시 | `S08~S10` 리포트 링크 + 판정 카드 |

## 3. 예외 시나리오 매트릭스
| 시나리오 | 실패 조건 | 기대 동작 | FAIL 기준 |
|---|---|---|---|
| X11-1 | 1.3x 폰트 스케일 | 핵심 숫자/CTA 잘림 없이 노출 | CTA 라벨 잘림, 단위 줄바꿈 분리 |
| X11-2 | Reduce Motion ON | 동일 의미 전달 + transform 축소 | 정보 누락/전환 혼동 |
| X11-3 | 저사양 스크롤 부하 | 카드 질감/전이 축소 후 입력 반응 유지 | 탭 반응 지연, 프레임 드랍 급증 |
| X11-4 | 외부 이동 실패 | fallback 3액션(복사/브라우저/취소) 2탭 이내 도달 | fallback 미노출/중복 다이얼로그 |
| X11-5 | 오프라인 결과 조회 | 오류 메시지 + 재시도 경로 + 이전 상태 보존 | 빈 화면/무응답 |

## 4. 성능/품질 예산
- CTA `tap -> visual feedback` P95: 100ms 이하
- 시트 `open -> stable` P95: 180ms 이하
- 리스트 스크롤 jank: 3% 이하 유지(`S08` 기준)
- 홈 첫 의미 렌더(P95): 1.2s 이하 유지(`S02` 기준)
- UI polish 적용 후 `device FAIL` 발생 시 릴리즈 보류(`S09`, `S10` 규칙 동일)

## 5. 판정 규칙
1. U1~U4 중 1개라도 FAIL이면 `조건부 진행` 이상으로 승격할 수 없다.
2. U1~U4 모두 PASS이고 `S10` 결론이 `진행`이면 최종 `진행`으로 판정한다.
3. U1~U4 PASS라도 `device FAIL`이면 최종 결론은 `보류`다.
4. `조건부 진행`의 경우 다음 루틴 TODO는 최대 3개로 제한해 즉시 실행 가능 상태로 등록한다.

## 6. 제출 템플릿
```md
# UI Quality Gate Evidence <date>
- Build: <git_sha_or_build_id>
- U1 Typography: <PASS/WARN/FAIL>
- U2 Visual Consistency: <PASS/WARN/FAIL>
- U3 Interaction Resilience: <PASS/WARN/FAIL>
- U4 Accessibility & Low-end: <PASS/WARN/FAIL>
- Linked Performance Verdict(S10): <진행/조건부 진행/보류>
- Final decision: <진행/조건부 진행/보류>
- Reasons:
  1) <근거 1>
  2) <근거 2>
  3) <근거 3>
- Next actions:
  1) <다음 루틴 액션 1>
  2) <다음 루틴 액션 2>
  3) <다음 루틴 액션 3>
```

## 7. 운영 체크포인트
- [ ] U1~U4 캡처/로그/체크리스트가 동일 빌드 기준으로 수집됐는가
- [ ] `S10` 결론과 `S11` 결론이 모순 없이 일치하는가
- [ ] 조건부/보류 사유가 `10-detailed-todo-board.md`에 등록됐는가
- [ ] Typography/Interaction 이슈가 `24`, `26`, `27` 규칙과 충돌하지 않는가

## 8. 문서 연동
- 모션/상호작용: `24-motion-and-interaction-playbook.md`
- 타이포/비주얼: `26-visual-typography-refresh.md`, `27-ui-visual-polish-pack.md`
- 하드닝 상위 계획: `33-reliability-and-performance-hardening-plan.md`
- 성능 게이트 체인: `37-performance-gate-calibration-spec.md`, `38-performance-gate-execution-template.md`, `39-performance-gate-evidence-package-spec.md`
- 릴리즈 체크리스트: `07-release-checklist.md`
- 실행 보드: `10-detailed-todo-board.md` `AY` 트랙

## 9. Cycle-54 통합 결론 연동
- `S11` 결과는 `41-unified-quality-verdict-package-spec.md`의 `S12` 입력(`V2`)으로 제출한다.

## 10. Cycle-55 드라이런/에스컬레이션 연동
- `S11` 드라이런/에스컬레이션 운영은 `42-unified-verdict-dryrun-and-escalation-spec.md`의 D2/D3 규칙을 따른다.

## 11. Cycle-56 이력/추세 연동
- `S11` 결론 이력은 `43-unified-verdict-history-and-trend-spec.md`의 H1/H2/H3 규칙으로 주간 관리한다.

## 12. Cycle-57 위험예산/프리즈 연동
- `S11` 조건부/보류 누적은 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 위험예산 규칙으로 관리한다.

## 13. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S11` 보류/조건부 진행 누적 시 프리즈 커뮤니케이션은 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 14. Cycle-59 프리즈 드릴/준비도 연동
- `S11` 프리즈 대응 검증은 `46-freeze-drill-readiness-score-spec.md` 드릴 기준으로 수행한다.

## 15. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S11` WARN/FAIL 후속 조치는 `47-freeze-drill-corrective-action-loop-spec.md`의 폐쇄 루프와 재개방 규칙으로 운영한다.

## 16. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S11` WARN/FAIL 후속 조치의 debt/blocked 판정은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
