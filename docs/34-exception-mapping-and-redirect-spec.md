# 예외 매핑/리다이렉트 상세 스펙 (S01, A05, AP-005)

## 1. 범위 확정(AP-005)
- 1차 노출 화면: `Home`
- 컴포넌트 위치: 상단 정보 카드 하단 CTA
- CTA 라벨: `공식 홈페이지에서 구매하기`
- 대상 URL: `https://dhlottery.co.kr`
- `S07-3` 확장(2026-02-26): `Settings` 화면에도 동일 CTA/모달/fallback 흐름 적용

## 2. 계측 키 확정
- 공통 이벤트: `interaction_cta_press`
- action 값:
  - `purchase_redirect_tap`
  - `purchase_redirect_confirm`
  - `purchase_redirect_fail`
  - `purchase_redirect_copy_link`
  - `purchase_redirect_open_browser`
  - `dismiss`(안내 모달 취소)
- 공통 파라미터:
  - `screen=home`
  - `component=purchase_redirect_cta` (고정)
  - `action=<위 값>`
  - 실패 시 `error_type=external_open_failed`, `url=https://dhlottery.co.kr`

## 3. 오류 매핑 표준(S01)
| error_type | 사용자 메시지 | 재시도 정책 | 로그 필드 |
|---|---|---|---|
| timeout | "응답이 지연되고 있어요. 잠시 후 다시 시도해 주세요." | 즉시 1회 + 지수 백오프 1회 | `timeout_ms`, `attempt`, `source` |
| http_4xx | "요청 형식 또는 접근 권한을 확인해 주세요." | 자동 재시도 없음 | `status_code`, `endpoint` |
| http_5xx | "서버가 일시적으로 불안정해요. 다시 시도해 주세요." | 2회 재시도 | `status_code`, `attempt` |
| schema | "데이터 형식이 달라 처리할 수 없어요." | 미러 API 1회 fallback | `schema_version`, `source` |
| storage_full | "저장 공간이 부족해 저장할 수 없어요." | 재시도 없음, 저장공간 안내 | `db_error`, `free_space_mb` |
| disk_io | "저장소 읽기/쓰기 오류가 발생했어요." | 즉시 1회 재시도 | `db_error`, `exception_class` |
| migration | "저장 데이터 업데이트가 필요해요." | 자동 재시도 없음, 앱 업데이트 안내 | `db_version`, `exception_class` |
| external_open_failed | "브라우저를 열 수 없어요." | fallback 액션 제공 | `url`, `exception_class` |
| unknown | "문제가 발생했어요. 다시 시도해 주세요." | 1회 재시도 | `exception_class`, `message` |

## 4. 리다이렉트 fallback 인터랙션(A05)
1. CTA 탭
- 1회 안내 모달 노출(성인 인증/이용시간/외부 이동)

2. 모달 확인
- 외부 브라우저 또는 Custom Tab 열기

3. 실패 시 공통 fallback 다이얼로그
- 액션:
  - `링크 복사`
  - `기본 브라우저로 열기`
  - `취소`(dismiss)
- 다이얼로그/버튼은 공통 컴포넌트(`ExternalOpenFallbackDialog`)로 재사용
- 외부 열기 실패 시 `purchase_redirect_fail` 이벤트 기록

## 5. 성능/안정성 가드(연동)
- Home에서 CTA 렌더/탭은 메인 스레드 블로킹 없이 즉시 반응(100ms 이내)
- 외부 이동 실패 fallback 시 중복 다이얼로그 표시 금지(싱글 인스턴스 가드)
- 백그라운드 복귀 후 모달/시트 상태 정합성 재검증

## 6. 검증 체크리스트
- Home에서 1탭으로 안내 모달까지 진입 가능한가
- 모달 확인 시 외부 이동이 정상 동작하는가
- 외부 이동 실패를 강제로 만들었을 때 fallback 3액션이 모두 노출되는가
- 계측 이벤트(action 값 5종)가 누락 없이 기록되는가

## 7. Cycle-45 운영 연동
- 사용자 여정 리허설/핫패스 성능 운영 기준은 `35-usecase-reliability-and-performance-playbook.md`를 따른다.

## 8. Cycle-47 구현 연동
- 계층별 구현 슬라이스/게이트/롤백 기준은 `36-hardening-implementation-slices.md`를 따른다.

## 9. Cycle-50 성능 게이트 연동
- 성능 게이트 캘리브레이션(측정 환경 분리/반복측정 규칙)은 `37-performance-gate-calibration-spec.md`를 따른다.

## 10. Cycle-51 운영 템플릿 연동
- 성능 게이트 실행/판정 템플릿은 `38-performance-gate-execution-template.md`를 따른다.

## 11. Cycle-52 증적 패키지 연동
- 성능 게이트 증적 패키지/최종 결론 템플릿은 `39-performance-gate-evidence-package-spec.md`를 따른다.

## 12. Cycle-53 UI 품질 게이트 연동
- `A05` fallback UI 판정은 `40-ui-quality-gate-and-interaction-resilience-spec.md`의 `S11-U3` 기준을 따른다.

## 13. Cycle-54 통합 결론 연동
- `A05` fallback 판정 결과는 `41-unified-quality-verdict-package-spec.md`의 `S12` 통합 결론 입력으로 제출한다.

## 14. Cycle-55 드라이런/에스컬레이션 연동
- `A05` fallback 실패 누적 시 에스컬레이션은 `42-unified-verdict-dryrun-and-escalation-spec.md`의 E13-2/E13-4 규칙을 따른다.

## 15. Cycle-56 이력/추세 연동
- `A05` fallback 실패 패턴은 `43-unified-verdict-history-and-trend-spec.md`의 주간 추세 카드(H2)에 포함한다.

## 16. Cycle-57 위험예산/프리즈 연동
- `A05` fallback 반복 실패는 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 코드별 한도/부분 프리즈 규칙을 따른다.

## 17. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- `A05` 관련 프리즈 이슈 공지/해제 판단은 `45-freeze-command-and-communication-playbook.md`를 따른다.

## 18. Cycle-59 프리즈 드릴/준비도 연동
- `A05` 관련 프리즈 대응 드릴은 `46-freeze-drill-readiness-score-spec.md` 시나리오에 포함한다.

## 19. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- `A05` fallback 관련 WARN/FAIL 후속 액션은 `47-freeze-drill-corrective-action-loop-spec.md` 폐쇄 루프 규칙으로 관리한다.

## 20. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- `A05` fallback 관련 액션의 debt 가중치 및 차단 정책은 `48-corrective-action-debt-and-release-block-spec.md` 기준으로 관리한다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
