# 유스케이스 기반 안정성/성능 운영 플레이북 (Cycle-45)

## 1. 목적
- 실제 사용자 여정 기준으로 예외 상황을 사전에 리허설하고, 복구 시간을 줄여 체감 안정성을 높인다.
- 성능 최적화를 "핫패스 우선순위"로 운영해 릴리즈 직전 품질 회귀를 줄인다.

## 2. 사용자 여정별 예외 리허설 카탈로그(S05)
| 여정 ID | 사용자 여정 | 대표 실패 조건 | 사용자 영향 | 기본 복구 전략 | 필수 계측/증적 |
|---|---|---|---|---|---|
| J01 | Home → 결과 조회 | API timeout / 5xx / schema mismatch | 결과 미조회, 이탈 | 1차 재시도 + 미러 fallback + 오류별 안내 | `ops_api_request`, 오류 타입별 UI 캡처 |
| J02 | Generator → 저장 | DB write 실패 / storage full | 번호 유실 우려 | 트랜잭션 롤백 + 저장공간 안내 + 재시도 제어 | `ops_storage_mutation`, 저장 실패 로그 |
| J03 | QR 스캔 → 등록 | 카메라 권한 거부 / 인식 실패 누적 | 등록 포기 | 권한 재요청 가이드 + 수동입력 전환 CTA | 권한 분기 로그, 수동 전환율 |
| J04 | Home → 공식 구매 이동 | Custom Tab/브라우저 열기 실패 | 구매 진입 단절 | fallback 3액션(복사/브라우저/취소) | `purchase_redirect_*`, fallback 동작 캡처 |
| J05 | 앱 재진입 | 프로세스 kill 후 상태 유실 | 맥락 손실, 재작업 | 안전 기본값 복구 + 상태 재검증 | 재진입 직후 상태 스냅샷 |
| J06 | 워치 → 폰 핸드오프 | 폰 미연결 / 딥링크 실패 | 흐름 중단 | 재시도 + 대체 진입 경로 안내 | 핸드오프 성공/실패 카운트 |

## 3. 리허설 운영 규칙
1. 실행 주기
- 주 1회(`S05`) 최소 4개 여정을 강제 재현한다.
- 릴리즈 주간에는 `J01`, `J02`, `J04`를 필수로 포함한다.

2. 판정 기준
- 실패 시 사용자 안내 문구가 1초 이내 노출되어야 한다.
- fallback 액션은 2탭 이내에 도달 가능해야 한다.
- 동일 실패 상황에서 중복 다이얼로그/시트가 2개 이상 뜨면 실패로 판정한다.

3. 산출물
- 실행 로그 1건 + 화면 캡처 1세트 + 재현 단계 1문단을 남긴다.
- 문서 저장 위치: `docs/assets/distribution/` 하위에 날짜 기준 파일명으로 보관한다.

## 4. 성능 최적화 운영안(S06)
## 4.1 핫패스 우선순위
| 우선순위 | 구간 | 목표 | 점검 포인트 |
|---|---|---|---|
| P0 | 앱 시작(Home 첫 렌더) | 콜드스타트 P95 2.2s 이하 | AppGraph 초기화, 시작 직후 I/O |
| P0 | Result 화면 진입 | 첫 의미 렌더 1.2s 이하 | 회차 전환 시 recomposition, 네트워크 중복 |
| P1 | Manage 리스트 스크롤 | jank 3% 이하 | 안정 key, 리스트 diff 비용 |
| P1 | 외부 이동 CTA 반응 | 탭 반응 100ms 이내 | 메인 스레드 블로킹 여부 |
| P2 | 장시간 세션 | ANR 0.2% 이하 유지 | 백그라운드 복귀/대량 데이터 처리 |

## 4.2 최적화 순서
1. 측정 고정: 동일 디바이스/동일 시나리오로 baseline 재수집
2. 병목 식별: startup trace와 화면 전환 trace를 분리 확인
3. 저비용 개선 우선: 불필요 recomposition/중복 호출 제거부터 적용
4. 회귀 차단: 임계치 초과 시 릴리즈 체크리스트 `S02/S04/S06` 항목 실패 처리

## 5. 문서/게이트 연동
- 상세 스펙 기준: `34-exception-mapping-and-redirect-spec.md`
- 상위 하드닝 계획: `33-reliability-and-performance-hardening-plan.md`
- 릴리즈 게이트: `07-release-checklist.md` 8장/9장
- 실행 보드: `10-detailed-todo-board.md` `AS` 트랙

## 6. Cycle-45 확정 항목
- `S05`: 사용자 여정 기반 예외 리허설 카탈로그/판정 규칙 확정
- `S06`: 핫패스 성능 최적화 우선순위 및 운영 순서 확정
- `S04` 연동: 성능 샘플 자동 수집 결과를 릴리즈 게이트 판정 입력으로 사용

## 7. Cycle-47 구현 연동
- `S05/S06` 운영 결과를 실제 구현 슬라이스로 넘길 때는 `36-hardening-implementation-slices.md` 기준을 따른다.

## 8. Cycle-50 캘리브레이션 연동
- `S06` 성능 운영 결과를 릴리즈 게이트로 연결할 때는 `37-performance-gate-calibration-spec.md`의 profile/반복측정 규칙을 따른다.

## 9. Cycle-51 운영 템플릿 연동
- `S05/S06` 실행 결과를 릴리즈 판정으로 넘길 때는 `38-performance-gate-execution-template.md`의 리포트/판정 트리를 따른다.

## 10. Cycle-52 증적 패키지 연동
- `S05/S06` 결과를 최종 릴리즈 결론으로 제출할 때는 `39-performance-gate-evidence-package-spec.md`의 패키지 구성을 따른다.

## 11. Cycle-53 UI 품질 게이트 연동
- `S05/S06` 실행 결과 중 UI 체감 이슈는 `40-ui-quality-gate-and-interaction-resilience-spec.md`의 U3/U4 체크포인트로 판정한다.

## 12. Cycle-54 통합 결론 연동
- `S05/S06` 결과는 `41-unified-quality-verdict-package-spec.md`의 `S12` 최종 결론 근거로 포함한다.

## 13. Cycle-55 드라이런/에스컬레이션 연동
- `S05/S06` 리허설 결과의 조건부/보류 케이스는 `42-unified-verdict-dryrun-and-escalation-spec.md` 기준으로 에스컬레이션한다.

## 14. Cycle-56 이력/추세 연동
- `S05/S06` 리허설 결과는 `43-unified-verdict-history-and-trend-spec.md`의 H1/H2 기준으로 주간 추세화한다.

## 15. Cycle-57 위험예산/프리즈 연동
- `S05/S06` 리허설 결과의 위험 누적은 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 예산 소진율 기준으로 판정한다.

## 16. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `S05/S06` 프리즈 상황 운영은 `45-freeze-command-and-communication-playbook.md`의 지휘/커뮤니케이션 규칙을 따른다.

## 17. Cycle-59 프리즈 드릴/준비도 연동
- `S05/S06` 프리즈 대응 숙련도는 `46-freeze-drill-readiness-score-spec.md`의 점수카드로 확인한다.

## 18. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `S05/S06` 리허설 결과에서 발생한 보정 액션은 `47-freeze-drill-corrective-action-loop-spec.md`의 폐쇄 게이트 기준으로 관리한다.

## 19. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `S05/S06` 리허설 결과 액션의 부채 점수/차단 판정은 `48-corrective-action-debt-and-release-block-spec.md`를 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
