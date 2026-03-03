# 안정성/완성도/성능 하드닝 계획 (v1)

## 1. 목적
- 실제 사용자 유스케이스에서 발생 가능한 예외 상황을 선제적으로 처리해 앱 완성도를 높인다.
- 크래시/ANR/지연 체감을 줄여 "빠르고 안정적"이라는 사용 인식을 확보한다.

## 2. 핵심 유스케이스와 실패 지점
| 유스케이스 | 실패 지점 | 목표 대응 |
|---|---|---|
| 결과 조회 | 공식 API 지연/실패/스키마 변경 | timeout + 재시도 + 미러 fallback + 오류 유형별 메시지 |
| 번호 저장 | 중복 저장/DB 쓰기 실패/저장공간 부족 | 중복 차단 + 트랜잭션 롤백 + 저장공간 부족 안내 |
| QR 스캔 | 카메라 권한 거부/저조도/인식 실패 누적 | 권한 재요청 가이드 + 저조도 가이드 + 수동 전환 CTA |
| 구매 페이지 이동 | 외부 브라우저 실패/URL 차단 | 1회 안내 모달 + 링크 복사 + 기본 브라우저 fallback |
| 앱 재진입 | 프로세스 죽음/상태 유실 | 핵심 화면 상태 복원 + 안전 기본값 복구 |
| 워치 핸드오프 | 폰 미연결/딥링크 실패 | 재시도 + 실패 안내 + 대체 진입 경로 제공 |

## 3. 예외처리 하드닝 체크리스트
1. 네트워크 계층
- 요청별 timeout 상한 통일(연결/읽기/전체)
- 재시도는 idempotent 요청만 허용(최대 횟수/백오프 고정)
- 오류 타입 분류(`timeout`, `http_4xx`, `http_5xx`, `schema`, `unknown`) 후 UI 문구 매핑

2. 데이터/저장소 계층
- 핵심 write 경로 트랜잭션 보장(부분 성공 금지)
- DB 예외(`SQLiteFullException`, `SQLiteDiskIOException`) 사용자 안내 규칙 추가
- 마이그레이션 실패 시 보호 경로(백업/복구/재초기화 안내) 명시

3. UI/상호작용 계층
- 버튼 연타/중복 제출 방지(로딩 중 재탭 무시)
- 외부 링크 실패 fallback(`복사`, `다시 시도`, `브라우저 선택`) 공통 컴포넌트화
- 백그라운드 복귀 시 시트/다이얼로그 상태 재검증

4. 운영/관측성 계층
- `ops_api_request`/`ops_storage_mutation` 기준 임계치 경보 규칙 추가
- 장애 리포트 템플릿(`15`)에 예외 타입/재현율/영향 범위 필수화

## 4. 성능 최적화 계획
## 4.1 목표 지표
- 콜드 스타트 P95: 2.2s 이하
- Home 첫 유의미 렌더(P95): 1.2s 이하
- 주요 화면 스크롤 jank 비율: 3% 이하
- ANR rate: 0.2% 이하
- Crash-free sessions: 99.7% 이상

## 4.2 최적화 트랙
| 트랙 | 내용 | 우선순위 |
|---|---|---|
| Startup | AppGraph 지연 초기화 유지 + 불필요 초기 작업 제거 | P0 |
| Rendering | 대형 리스트 recomposition 최소화, 안정 key/derived state 점검 | P0 |
| I/O | 메인 스레드 I/O 금지 점검, DB 인덱스/쿼리 플랜 점검 | P0 |
| Network | 중복 호출 합치기, 캐시 TTL 규칙 재정의 | P1 |
| Memory | 비트맵/리소스 누수 점검, 저사양 메모리 압박 시 fallback | P1 |
| Benchmark | Macrobenchmark/Baseline Profile 주기 실행 | P1 |

## 5. 테스트/게이트 반영
- `06-test-plan.md`에 예외처리/성능 회귀 시나리오 추가
- `07-release-checklist.md`에 성능 게이트(스타트업/jank/ANR) 점검 항목 추가
- 주기 루틴: `run-ops-observability-check` + 성능 샘플 체크를 같은 배포 체인으로 점검

## 6. 단계별 실행안
1. Phase-1 (이번 주)
- 예외 매트릭스 기준 공통 에러 매핑 표준 확정
- 구매 리다이렉트 fallback 공통 규칙 확정

2. Phase-2 (다음 주)
- 성능 지표 자동 수집 스크립트 추가(스타트업/jank 샘플)
- 임계치 초과 시 경고 리포트 생성

3. Phase-3 (다다음 주)
- Macrobenchmark + Baseline Profile 루틴 정착
- 릴리즈 게이트에 성능/안정성 결과 통합

## 7. Cycle-42 우선 실행 확정
- 1차 실행 2개:
  - `S01`: 네트워크/저장소 오류 타입 매핑 표준화
  - `A05`: 외부 링크 리다이렉트 실패 fallback 공통화
- 선정 기준:
  - 사용자 실패 체감에 직접 영향
  - 구현 난이도 대비 안정성 개선 효과가 큼

## 8. Cycle-44 상세 스펙 연동
- 상세 기준 문서: `34-exception-mapping-and-redirect-spec.md`
- 연동 항목:
  - `S01`: 오류 타입별 메시지/재시도/로그 필드 표준
  - `A05`: 리다이렉트 실패 fallback 인터랙션 및 계측 action 키
  - `AP-005`: Home 1차 노출 범위 확정

## 9. Cycle-45 운영 플레이북 연동
- 운영 플레이북 문서: `35-usecase-reliability-and-performance-playbook.md`
- 연동 항목:
  - `S05`: 사용자 여정(J01~J06) 기반 예외 리허설 카탈로그/판정 기준
  - `S06`: 핫패스(Home/Result/Manage) 성능 최적화 우선순위 운영
  - `S04`: 성능 샘플 자동 수집 결과를 릴리즈 게이트 판정 입력으로 연결

## 10. Cycle-47 구현 슬라이스 연동
- 구현 슬라이스 문서: `36-hardening-implementation-slices.md`
- 연동 항목:
  - `S07-1~S07-3`: 네트워크/저장소 오류 매핑 + fallback 공통 컴포넌트 구현 기준
  - `S07-4`: 성능 자동 수집 리포트의 게이트 입력 연동 기준
  - `S07-5`: 임계치 초과 시 릴리즈 보류/롤백 판정 기준

## 11. Cycle-50 성능 게이트 캘리브레이션 연동
- 캘리브레이션 문서: `37-performance-gate-calibration-spec.md`
- 연동 항목:
  - `S08`: emulator/device 측정 프로파일 분리 + 반복측정(N=5) 판정 기준
  - `S02`: 절대 임계치 기반 성능 목표(실기기 프로파일 기준)
  - `S04/S07-4`: 자동 수집 리포트 포맷/필수 필드 표준화
  - `S07-5`: `device FAIL` 시 릴리즈 보류 규칙 확정

## 12. Cycle-51 성능 게이트 운영 템플릿 연동
- 운영 템플릿 문서: `38-performance-gate-execution-template.md`
- 연동 항목:
  - `S09`: 실행 커맨드 매트릭스(emulator/device) 표준화
  - `S09`: 리포트 필수 필드/파일명 템플릿 고정
  - `S09`: 판정 트리(`device FAIL` 보류, `emulator FAIL + device PASS` 최적화 백로그) 운영 기준 확정

## 13. Cycle-52 성능 게이트 증적 패키지 연동
- 증적 패키지 문서: `39-performance-gate-evidence-package-spec.md`
- 연동 항목:
  - `S10`: E1~E5 증적 패키지 구성 표준화
  - `S10`: 최종 결론(진행/조건부 진행/보류) 제출 템플릿 고정
  - `S10`: 판정 근거와 후속 액션을 루틴 TODO로 연결하는 체크포인트 확정

## 14. Cycle-53 UI 품질 게이트 연동
- UI 품질 게이트 문서: `40-ui-quality-gate-and-interaction-resilience-spec.md`
- 연동 항목:
  - `S11`: U1~U4(타이포/비주얼/상호작용/접근성) 운영 게이트 확정
  - `S11`: U5를 통해 `S08~S10` 성능 결론과 UI 결론을 단일 판정으로 정렬

## 15. Cycle-54 통합 결론 패키지 연동
- 통합 결론 문서: `41-unified-quality-verdict-package-spec.md`
- 연동 항목:
  - `S12`: `S10`(성능) + `S11`(UI) 단일 결론 규칙(R1~R4) 확정
  - `S12`: 조건부/보류 사유를 다음 루틴 TODO로 강제 연결

## 16. Cycle-55 드라이런/에스컬레이션 연동
- 운영 문서: `42-unified-verdict-dryrun-and-escalation-spec.md`
- 연동 항목:
  - `S13`: 최근 2개 빌드 기준 통합 결론 드라이런(D1~D5) 고정
  - `S13`: 에스컬레이션 코드(E13-1~E13-4) + SLA(15/20/10분) 운영 기준 확정

## 17. Cycle-56 이력/추세 연동
- 운영 문서: `43-unified-verdict-history-and-trend-spec.md`
- 연동 항목:
  - `S14`: 통합 결론 이력 레지스트리(H1) + 주간 추세 카드(H2) 운영
  - `S14`: 반복 에스컬레이션 클러스터(H3) 기반 우선순위 승격 기준 확정

## 18. Cycle-57 위험예산/프리즈 연동
- 운영 문서: `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`
- 연동 항목:
  - `S15`: 통합 결론 위험예산/프리즈/해제/예외 승인 규칙 확정
  - `S15`: 주간 리스크 리포트와 릴리즈 제어 정책 연결

## 19. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- 운영 문서: `45-freeze-command-and-communication-playbook.md`
- 연동 항목:
  - `S16`: 프리즈 지휘 체계(RACI), 커뮤니케이션, 해제 회의, 사후 회고 규칙 확정

## 20. Cycle-59 프리즈 드릴/준비도 연동
- 운영 문서: `46-freeze-drill-readiness-score-spec.md`
- 연동 항목:
  - `S17`: 프리즈 드릴 시나리오/준비도 점수/합격 기준 확정

## 21. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- 운영 문서: `47-freeze-drill-corrective-action-loop-spec.md`
- 연동 항목:
  - `S18`: WARN/FAIL 보정 액션 등록/폐쇄 게이트/재개방 규칙/SLA 운영 기준 확정

## 22. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- 운영 문서: `48-corrective-action-debt-and-release-block-spec.md`
- 연동 항목:
  - `S19`: 보정 액션 부채 점수/차단·해제 임계치/예외 승인 운영 기준 확정

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
