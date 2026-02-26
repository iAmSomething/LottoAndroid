# 개발 문서 운영 인덱스

## 일정 기준 문서 사용 맵
| 기간 (2026) | 주차 | 핵심 목표 | 반드시 사용하는 문서 |
|---|---|---|---|
| 02-26 ~ 03-04 | W1 | 환경/기반/아키텍처 고정 | 00, 01, 02, 09, 10 |
| 03-05 ~ 03-11 | W2 | 번호생성/저장 안정화 | 03, 04, 05, 09, 10 |
| 03-12 ~ 03-18 | W3 | QR 등록(카메라 스캔) 완성 | 03, 04, 05, 06, 09, 10 |
| 03-19 ~ 03-25 | W4 | 당첨 API/채점 정확도 확보 | 04, 05, 06, 09, 10 |
| 03-26 ~ 04-01 | W5 | 알림/설정 플로우 완성 | 03, 05, 06, 09, 10 |
| 04-02 ~ 04-08 | W6 | 위젯 A/B 동작 완성 | 03, 05, 06, 08, 09, 10 |
| 04-09 ~ 04-15 | W7 | 통계/회귀 테스트 강화 | 05, 06, 09, 10, 11 |
| 04-16 ~ 04-22 | W8 | 릴리즈 준비/인수인계 | 06, 07, 09, 10, 11 |
| 04-23 ~ 05-20 | W9~W12 | 고도화/플랫폼 확장 | 09, 10, 16, 20, 21, 22, 23 |
| 04-30 ~ 05-20 | W10~W12 | 모션/상호작용 고도화 | 08, 10, 22, 23, 24 |

## 문서별 역할
- `00-product-overview.md`: 제품 목적/범위 고정 기준
- `01-environment-setup.md`: 환경 및 실행 기준
- `02-architecture.md`: 구조/의존/확장 기준
- `03-user-flow.md`: 화면 간 E2E 흐름 기준
- `04-data-api-spec.md`: 데이터 모델/외부 연동 계약 기준
- `05-epics-and-tasks.md`: 에픽 단위 상위 백로그
- `06-test-plan.md`: 테스트 시나리오 및 합격 기준
- `07-release-checklist.md`: 릴리즈 직전 체크리스트
- `08-design-mapping.md`: 디자인 반영 정합성 기준
- `09-development-schedule.md`: 일정/마일스톤/완료 기준
- `10-detailed-todo-board.md`: 실제 실행용 상세 TODO 보드
- `11-progress-tracker.md`: 일일 진행/블로커/결정 로그
- `12-store-metadata.md`: 스토어 설명/릴리즈노트 초안
- `13-privacy-and-permissions.md`: 권한/개인정보 고지 초안
- `14-signing-and-distribution.md`: 서명키/릴리즈 빌드 절차
- `15-incident-response-runbook.md`: 장애 대응 루틴/템플릿
- `16-next-sprint-backlog.md`: 다음 스프린트 우선순위 백로그
- `17-release-preflight-report.md`: 자동 프리플라이트 점검 리포트
- `18-device-validation-report.md`: 실기기 계측 테스트 실행 이력
- `19-offline-design-qa-checklist.md`: Figma 제한 시 오프라인 디자인 정합성 점검 가이드
- `20-wearos-integration-plan.md`: Wear OS(갤럭시 워치) 연동 전략/아키텍처/단계별 실행안
- `21-product-enhancement-ideas.md`: 앱 전체 고도화 아이디어 풀
- `22-prioritization-matrix.md`: 고도화 우선순위 점수/실행 권고
- `23-kpi-and-experiment-plan.md`: KPI 목표/실험 설계/성공 기준
- `24-motion-and-interaction-playbook.md`: 스플래시/내부 모션 규격/QA 기준
- `25-routine-cycle-report.md`: 사이클 단위 코드/품질/UX 진단 리포트

## 운영 규칙
1. 작업 시작 전 `09`, `10`, `11`을 먼저 확인한다.
2. 기능 머지 전 `06` 테스트 항목을 최소 1회 이상 수행한다.
3. 주차 종료 시 `11`에 완료/미완료/리스크를 반드시 기록한다.
