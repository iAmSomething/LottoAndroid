# 성능 게이트 캘리브레이션 스펙 (Cycle-50)

## 1. 목적
- `S02` 성능 게이트의 오탐을 줄이고, 실제 배포 판정에 사용할 수 있는 기준으로 보정한다.
- 에뮬레이터/실기기 측정값을 분리해 품질 판단 일관성을 확보한다.

## 2. 현재 이슈 요약
- 2026-02-26 샘플에서 에뮬레이터 기준 `Startup/Jank`가 임계치 초과로 FAIL.
- 단일 샘플 1회 측정값은 변동폭이 커서 릴리즈 판정 신뢰도가 낮다.

## 3. 캘리브레이션 규칙(S08)
1. 측정 환경 분리
- `profile=emulator`: 회귀 감시용(트렌드 비교), 배포 차단 근거로 단독 사용 금지
- `profile=device`: 릴리즈 판정용(실기기 기준), 배포 차단 근거로 사용

2. 반복 측정 규칙
- 각 프로파일 최소 `N=5` 반복 측정
- 중앙값(median) + P95를 함께 기록
- 최초 1회는 warm-up 실행으로 본 집계에서 제외

3. 판정 규칙
- 에뮬레이터: 이전 2주 baseline 대비 악화율 기준으로 `WARN/FAIL` 판정
- 실기기: 절대 임계치 기준(`Startup P95`, `jank`, `ANR`)으로 `PASS/FAIL` 판정
- 릴리즈 차단은 `device FAIL`일 때만 적용한다.

4. 리포트 규칙
- 리포트 파일명: `performance_gate_<profile>_<yyyy-mm-dd>.md`
- 필수 항목:
  - 측정 기기/OS/API
  - 반복 횟수/제외 샘플 수
  - Startup median/P95
  - Jank median/P95
  - ANR count
  - 최종 판정(PASS/WARN/FAIL) + 근거

## 4. 게이트 연결 규칙
- `S02`: 성능 목표 자체(절대 기준)
- `S04`: 샘플 자동 수집 파이프라인(리포트 생성)
- `S07-4`: 체크리스트 연동/자동 판정 입력
- `S07-5`: FAIL 시 보류/롤백 판정 실행

## 5. 실행 단계
1. 스크립트 확장
- `run-performance-sample-check.sh`에 `--profile emulator|device`, `--repeat`, `--warmup` 옵션을 추가한다.

2. 리포트 집계
- 반복 측정 결과를 표준 포맷으로 저장하고 baseline 대비 비교 값을 자동 계산한다.

3. 체크리스트 연동
- `07-release-checklist.md`에서 `S02`, `S07-4`, `S07-5` 항목이 동일 리포트를 참조하도록 정렬한다.

## 6. 리스크/완화
- 리스크: 실기기 미보유 시 `device` 프로파일 측정이 지연될 수 있음
- 완화: 에뮬레이터는 추세 모니터링으로 유지하고, 릴리즈 차단 판단은 보수적으로 `pending` 처리

## 7. 문서 연동
- 상위 계획: `33-reliability-and-performance-hardening-plan.md`
- 운영 플레이북: `35-usecase-reliability-and-performance-playbook.md`
- 구현 슬라이스: `36-hardening-implementation-slices.md`
- 실행 보드: `10-detailed-todo-board.md` `AV` 트랙

## 8. Cycle-51 운영 템플릿 연동
- 실제 실행 커맨드/리포트/판정 트리 템플릿은 `38-performance-gate-execution-template.md`를 따른다.

## 9. Cycle-52 증적 패키지 연동
- 캘리브레이션 결과 제출 패키지는 `39-performance-gate-evidence-package-spec.md`를 따른다.

## 10. Cycle-53 UI 품질 게이트 연동
- `S08` 결과는 `40-ui-quality-gate-and-interaction-resilience-spec.md`의 U5 입력으로 사용한다.

## 11. Cycle-54 통합 결론 연동
- `S08` 캘리브레이션 판정은 `41-unified-quality-verdict-package-spec.md`의 `S12` 입력(`V1`)으로 사용한다.

## 12. Cycle-55 드라이런/에스컬레이션 연동
- `S08` 판정의 보류 케이스는 `42-unified-verdict-dryrun-and-escalation-spec.md`의 E13-1 규칙으로 즉시 이관한다.

## 13. Cycle-56 이력/추세 연동
- `S08` 판정 이력은 `43-unified-verdict-history-and-trend-spec.md`의 H1/H2 지표로 주간 집계한다.

## 14. Cycle-57 위험예산/프리즈 연동
- `S08` 결과가 반복 `FAIL`로 누적될 경우 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 전체 프리즈 규칙을 적용한다.

## 15. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S08` 반복 실패 프리즈 상태의 커뮤니케이션/해제 회의는 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 16. Cycle-59 프리즈 드릴/준비도 연동
- `S08` 반복 실패 시 프리즈 대응 준비도는 `46-freeze-drill-readiness-score-spec.md` 기준으로 평가한다.

## 17. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S08` 반복 실패 이후 보정 액션 운영은 `47-freeze-drill-corrective-action-loop-spec.md` 폐쇄 루프 기준으로 진행한다.

## 18. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S08` 반복 실패 액션의 debt/blocked 판정은 `48-corrective-action-debt-and-release-block-spec.md` 기준으로 진행한다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
