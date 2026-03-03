# 성능 게이트 실행 템플릿/판정 트리 (Cycle-51)

## 1. 목적
- `S08` 캘리브레이션 기준을 실제 운영 절차로 고정한다.
- 루틴 실행자가 동일한 커맨드/포맷/판정 규칙으로 결과를 남기도록 표준화한다.

## 2. 실행 커맨드 매트릭스(S09)
| 프로파일 | 목적 | 필수 옵션 | 결과 파일 |
|---|---|---|---|
| emulator | 회귀 추세 감시 | `--profile emulator --repeat 5 --warmup 1` | `docs/assets/distribution/performance_gate_emulator_<date>.md` |
| device | 릴리즈 판정 | `--profile device --repeat 5 --warmup 1` | `docs/assets/distribution/performance_gate_device_<date>.md` |

### 실행 예시
```bash
./scripts/run-performance-sample-check.sh \
  --profile emulator \
  --repeat 5 \
  --warmup 1 \
  --serial emulator-5554 \
  --save-report docs/assets/distribution/performance_gate_emulator_2026-02-26.md
```

```bash
./scripts/run-performance-sample-check.sh \
  --profile device \
  --repeat 5 \
  --warmup 1 \
  --serial <physical-device-serial> \
  --save-report docs/assets/distribution/performance_gate_device_2026-02-26.md
```

## 3. 리포트 템플릿
| 항목 | 값 |
|---|---|
| Profile | emulator/device |
| Device | 모델명, OS, API |
| Runs | total / warm-up excluded |
| Startup | median, P95 |
| Jank | median, P95 |
| ANR | count |
| Baseline 비교 | 악화율/개선율(%) |
| Final Verdict | PASS/WARN/FAIL |
| Notes | 특이사항/블로커 |

## 4. 판정 트리
1. `profile=device`인가?
- 예: 절대 임계치 기준으로 PASS/FAIL 판정
- 아니오: baseline 대비 악화율 기준 WARN/FAIL 판정

2. `device FAIL`인가?
- 예: 릴리즈 보류 + `S07-5` 판정 로그 기록
- 아니오: 다음 게이트 진행

3. `emulator FAIL`이고 `device PASS`인가?
- 예: 최적화 백로그 등록(`S06/S08`) 후 릴리즈 진행 가능
- 아니오: 기존 판정 유지

## 5. 운영 체크리스트(S09)
- [ ] emulator/device 리포트가 같은 날짜 기준으로 생성되었는가
- [ ] 두 리포트 모두 반복측정 수와 제외 샘플 수가 기록되었는가
- [ ] `device` 판정이 릴리즈 보류 여부에 반영되었는가
- [ ] `WARN/FAIL` 원인이 다음 루틴 TODO로 연결되었는가

## 6. 문서 연동
- 캘리브레이션 기준: `37-performance-gate-calibration-spec.md`
- 릴리즈 게이트: `07-release-checklist.md`
- 하드닝 상위 계획: `33-reliability-and-performance-hardening-plan.md`
- 증적 패키지 표준: `39-performance-gate-evidence-package-spec.md`
- 실행 보드: `10-detailed-todo-board.md` `AW` 트랙

## 7. Cycle-53 UI 품질 게이트 연동
- `S09` 리포트 결과는 `40-ui-quality-gate-and-interaction-resilience-spec.md`의 최종 결론 입력으로 함께 제출한다.

## 8. Cycle-54 통합 결론 연동
- `S09` 실행 리포트는 `41-unified-quality-verdict-package-spec.md`의 `S12` 통합 결론 작성 시 `V1` 근거로 사용한다.

## 9. Cycle-55 드라이런/에스컬레이션 연동
- `S09` 리포트는 `42-unified-verdict-dryrun-and-escalation-spec.md`의 D1 드라이런 입력으로 사용한다.

## 10. Cycle-56 이력/추세 연동
- `S09` 리포트 결과는 `43-unified-verdict-history-and-trend-spec.md`의 주간 추세 산출 입력으로 사용한다.

## 11. Cycle-57 위험예산/프리즈 연동
- `S09` 실행 결과는 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 주간 리스크 리포트 입력으로 포함한다.

## 12. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S09` 실행 결과 기반 프리즈 상태 업데이트는 `45-freeze-command-and-communication-playbook.md` 템플릿으로 기록한다.

## 13. Cycle-59 프리즈 드릴/준비도 연동
- `S09` 실행 결과 기반 프리즈 드릴 증적은 `46-freeze-drill-readiness-score-spec.md` 템플릿으로 관리한다.

## 14. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S09` 실행 결과에서 파생된 보정 액션 추적/폐쇄는 `47-freeze-drill-corrective-action-loop-spec.md` 운영 템플릿을 따른다.

## 15. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S09` 실행 결과 액션의 debt 산출/차단 판정은 `48-corrective-action-debt-and-release-block-spec.md` 운영 템플릿을 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
