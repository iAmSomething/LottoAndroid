# 하드닝 구현 슬라이스/게이트 스펙 (Cycle-47)

## 1. 목적
- 이미 확정된 하드닝 기획(`33`, `34`, `35`)을 실제 구현 순서로 전환한다.
- 코드 충돌을 줄이기 위해 계층별 적용 범위와 완료 기준(DoD)을 명확히 고정한다.

## 2. 구현 슬라이스 맵(S07)
| Slice ID | 영역 | 범위 | 산출물 | 완료 기준 |
|---|---|---|---|---|
| S07-1 | 네트워크 오류 매핑 | timeout/4xx/5xx/schema/unknown 분류 표준 적용 | 오류 분류 유틸 + 호출부 매핑 표 | `S01` 오류 타입이 일관된 키로 로깅/UI 노출 |
| S07-2 | 저장소 오류 매핑 | storage_full/disk_io/migration 분기 표준 적용 | 저장소 예외 매퍼 + 사용자 메시지 규칙 | 저장 실패 시 중복 안내 없이 1회 메시지 노출 |
| S07-3 | 리다이렉트 fallback 공통화 | Home 구현을 공용 컴포넌트로 승격, Result/Settings 확장 가능 구조 | fallback sheet/dialog 공통 UI 규격 | `A05` 3액션(복사/브라우저/취소) 재사용 가능 |
| S07-4 | 성능 샘플 수집 파이프라인 | startup/jank/ANR 리포트 자동 수집→게이트 입력 연결 | 스크립트 결과 리포트 + 판정 규칙 | 릴리즈 체크리스트에서 자동 수집 결과 참조 가능 |
| S07-5 | 릴리즈 판정/롤백 규칙 | 임계치 초과 시 배포 보류/롤백 판단 절차 | 판정 템플릿 + 롤백 트리거 | 임계치 초과 시 "진행/보류"가 1개 문서로 결정 가능 |

## 3. 계층별 적용 기준
1. Domain
- 오류 타입 enum/키를 단일 소스로 유지한다.
- UI/로그가 참조하는 문자열 키를 상수화한다.

2. Data
- 네트워크/저장소 예외를 공통 오류 타입으로 변환한다.
- `source`, `attempt`, `latency` 필드는 누락 없이 남긴다.

3. UI
- 오류 메시지는 "원인 + 다음 행동" 1문장 구조를 유지한다.
- fallback 모달/시트는 화면별 중복 구현을 금지하고 공용 컴포넌트로 통일한다.

4. Ops
- `ops_api_request`, `ops_storage_mutation`, `purchase_redirect_*`를 한 번에 점검 가능한 리포트 구조로 묶는다.
- 성능 리포트는 릴리즈 체크리스트 항목과 동일한 이름(`S02`, `S04`, `S06`)으로 저장한다.

## 4. 예외 유스케이스 기반 검증 항목
| Case | 재현 조건 | 기대 동작 | 실패 판정 |
|---|---|---|---|
| C01 timeout 연속 발생 | 결과 조회 요청 2회 연속 timeout | 사용자 안내 + 1회 재시도 후 종료 | 무한 재시도 또는 무응답 |
| C02 storage_full | 저장공간 부족 상태에서 저장 시도 | 저장 실패 안내 + 재시도 유도 | 앱 멈춤/중복 토스트 |
| C03 redirect open 실패 | Custom Tab/브라우저 강제 실패 | fallback 3액션 노출 | fallback 미노출 |
| C04 process kill 복귀 | 백그라운드 후 프로세스 재생성 | 핵심 상태 복구 + 안전 기본값 | 이전 시트/다이얼로그 잔존 |
| C05 jank 임계 초과 | Manage 리스트 스크롤 부하 | 성능 리포트 경고 + 게이트 보류 | 임계 초과인데 배포 진행 |

## 5. 성능 최적화 적용 순서
1. Startup
- 앱 시작 직후 I/O와 초기화 항목을 분리하고 블로킹 경로를 제거한다.

2. Result
- 회차 전환 시 중복 호출/불필요 recomposition을 우선 제거한다.

3. Manage
- 리스트 key 안정성, diff 비용, 스크롤 jank를 우선 점검한다.

4. Release Gate
- 성능 리포트를 릴리즈 체크리스트에 첨부하고 임계치 초과 시 배포 보류를 기본값으로 한다.

## 6. 롤백/보류 결정 규칙
- `P0` 지표(콜드스타트 P95, ANR, 크래시 프리) 중 1개라도 임계치 초과 시 릴리즈 보류
- `P1` 지표(jank, fallback 실패율) 2개 이상 초과 시 릴리즈 보류
- 롤백 시 우선순위:
  1. 신규 공통 fallback 컴포넌트
  2. 저장소 오류 매핑 변경
  3. 네트워크 오류 매핑 변경

## 7. 문서 연동
- 상위 계획: `33-reliability-and-performance-hardening-plan.md`
- 상세 스펙: `34-exception-mapping-and-redirect-spec.md`
- 운영 플레이북: `35-usecase-reliability-and-performance-playbook.md`
- 실행 보드: `10-detailed-todo-board.md` `AT` 트랙

## 8. Cycle-50 보정 연동
- 성능 게이트 보정 규칙(`S08`)은 `37-performance-gate-calibration-spec.md`를 따른다.

## 9. Cycle-51 운영 연동
- 성능 게이트 실행 템플릿(`S09`)은 `38-performance-gate-execution-template.md`를 따른다.

## 10. Cycle-52 증적 연동
- 성능 게이트 증적 패키지(`S10`)는 `39-performance-gate-evidence-package-spec.md`를 따른다.

## 11. Cycle-53 UI 품질 게이트 연동
- `S07` 슬라이스 적용 후 UI 회귀 평가는 `40-ui-quality-gate-and-interaction-resilience-spec.md`의 U1~U4로 추가 검증한다.

## 12. Cycle-54 통합 결론 연동
- `S07` 적용 결과는 `41-unified-quality-verdict-package-spec.md`의 `S12` 결론 산출 시 필수 근거로 포함한다.

## 13. Cycle-55 드라이런/에스컬레이션 연동
- `S07` 회귀 이슈의 보류/조건부 진행 처리는 `42-unified-verdict-dryrun-and-escalation-spec.md`의 D3/D5 규칙을 따른다.

## 14. Cycle-56 이력/추세 연동
- `S07` 회귀 추세는 `43-unified-verdict-history-and-trend-spec.md`의 반복 이슈 클러스터(H3) 기준으로 관리한다.

## 15. Cycle-57 위험예산/프리즈 연동
- `S07` 회귀 누적이 예산 한도를 초과하면 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 프리즈 트리거를 적용한다.

## 16. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S07` 프리즈 발동 시 대응 흐름은 `45-freeze-command-and-communication-playbook.md` 기준으로 운영한다.

## 17. Cycle-59 프리즈 드릴/준비도 연동
- `S07` 프리즈 대응 검증은 `46-freeze-drill-readiness-score-spec.md` 드릴 절차를 따른다.

## 18. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S07` 기반 프리즈 이슈 후속 조치는 `47-freeze-drill-corrective-action-loop-spec.md`의 등록/폐쇄/재개방 규칙을 따른다.

## 19. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S07` 기반 프리즈 이슈의 부채 집계 및 릴리즈 차단 규칙은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
