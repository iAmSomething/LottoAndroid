# UI 비주얼 폴리시 팩 (v1)

## 1. 목적
- 기능 완성도와 별개로 남아있는 "기본 앱 느낌"을 제거하고, 브랜드 인상을 강화한다.
- 타이포 리프레시(v1)를 기반으로 카드/배경/아이콘/레이아웃 완성도를 2차로 끌어올린다.

## 2. 핵심 문제
- 폰트는 토큰화되었지만 실제 브랜드 폰트 자산이 없어 시각 개성이 약함
- 카드/버튼/칩이 기능별로 다르게 보여 일관된 룩앤필 부족
- 화면 배경이 평평해 정보는 읽히지만 분위기가 없음

## 3. 아트 디렉션
- Theme: `Lucky Editorial 2.0`
- Mood: 신뢰감 + 행운감(과장 없이 단단한 톤)
- 방향:
  - 강한 숫자 대비
  - 따뜻한 배경 + 명확한 카드 레이어
  - 제한된 포인트 컬러(남발 금지)

## 4. 화면별 폴리시 포인트
## 4.1 Home
- 회차 카드 배경을 단색에서 2단 레이어(그라디언트 + 얕은 텍스처)로 전환
- 주간 리포트 카드의 숫자 행을 테이블형으로 정렬해 가독성 강화
- 상단 CTA 2개 중 핵심 1개만 강한 강조 유지

## 4.2 Result
- 당첨번호/보너스/합계 당첨금을 숫자 중심 수직 위계로 재배치
- 상태 배지(당첨/낙첨)의 컬러 채도 차이를 정규화
- 회차 시트 선택행 대비를 높여 "현재 선택"을 즉시 인지 가능하게 조정

## 4.3 Generator/Manage
- BallChip 라운드/테두리/채움 규칙 통일
- 카드 메타(회차/날짜/상태)를 2행 구조로 정리해 복잡도 감소
- 편집/저장/적용 버튼의 우선순위를 색 대비로 통일

## 4.4 Wear
- 원형 화면 텍스트 블록을 3단(숫자/상태/보조)으로 고정
- 작은 화면에서는 메타 문구를 먼저 축소해 핵심 수치 가독성 유지

## 5. 컴포넌트 규칙
- Card: 기본/강조/위험 3종으로 제한
- Button: Primary/Secondary/Text 3종으로 제한
- Icon: 24dp, 동일 스트로크 두께, 동일 모서리 라운드 규칙

## 6. 증적 기준
- Home/Result/Generator/Manage 전후 스크린샷 2세트
- 낮/어두운 배경에서 대비 점검 캡처
- 1.0x/1.3x 폰트 스케일 비교 캡처

## 7. 작업 보드 매핑
- `10-detailed-todo-board.md`의 `AB-001 ~ AB-010`으로 추적한다.

## 8. Cycle-15 실행 순서
1. AB-005/AB-006: 실폰트 자산/매핑 먼저 완료
2. AB-007/AB-008: 카드 레이어/아이콘 규격 폴리시 적용
3. AB-009/AB-010: 전후 캡처 + 접근성/저조도 QA 증적 수집

## 9. Cycle-22 시각 완성도 고도화안
- 타이포 시안 2안 동시 운영
  - A안 `Trust`: 굵기 대비를 줄이고 정보 안정감 강화
  - B안 `Editorial`: 숫자/헤드라인 대비를 키워 임팩트 강화
- 숫자 블록 미세 규칙
  - 회차/당첨금/카운트 영역은 숫자와 단위 자간을 분리
  - 1줄 우선, 2줄 전환 시 단위 줄바꿈 금지
- 스플래시 시그니처 모션
  - 기본 모드: 700~900ms 범위 안에서 브랜드 볼 모티프 1회 전이
  - Reduce Motion: 동일 정보 밀도 유지, 페이드 중심 200~300ms
- 카드 표면 질감 실험
  - 강도 0/1/2 3단계로 제한해 "과한 장식" 리스크 차단
  - Home/Result 핵심 카드만 적용하고 나머지는 평면 유지

## 10. 승인 패키지(`AJ-005`) 산출물
- 체크리스트: 폰트 파일/라이선스/보관 경로/적용 순서
- 운영 증적: Firebase 배포 dry-run 또는 실배포 로그 1건
- 시각 증적: A/B 타이포 시안 캡처(Home/Result 각 1장)
- 품질 조건: 1.3x 폰트 스케일, 저조도 대비, Reduce Motion 동등성 확인

## 11. Cycle-23 증적 매트릭스(`AK-005`)
- 대상 화면: Home, Result, Generator, Manage
- 캡처 조건:
  - 기본 밝기 + 폰트 1.0x
  - 기본 밝기 + 폰트 1.3x
  - 저조도(다크 환경) + 폰트 1.0x
  - 저조도(다크 환경) + 폰트 1.3x
- 판정 기준:
  - 숫자/단위 분리 가독성 유지(줄바꿈 시 단위 분리 금지)
  - 핵심 CTA 1개 강조 원칙 유지(강조 컬러 중복 금지)
  - 아이콘 스트로크/라운드 일관성 유지(혼합 스타일 없음)

## 12. Cycle-24 실행 운영안(`AL-005`)
- 캡처 파일명 규칙:
  - `{screen}_{brightness}_{fontScale}_{variant}.png`
  - 예시: `home_normal_1_0_a.png`, `result_lowlight_1_3_b.png`
- 담당 구분:
  - 기획: 캡처 시나리오/판정 체크리스트 작성
  - 구현: 캡처 생성 및 파일 업로드
  - 리뷰: 판정 기준 충족 여부 확인(`pass`/`revise`)
- 완료 판정 템플릿:
  - `Screen`: Home|Result|Generator|Manage
  - `Variant`: A|B
  - `Conditions`: normal-1.0 / normal-1.3 / lowlight-1.0 / lowlight-1.3
  - `Decision`: pass|revise
  - `Notes`: 줄바꿈/대비/아이콘 일관성 이슈

## 13. Cycle-25 산출물 인벤토리 템플릿(`AM-005`)
- 목적:
  - `AJ-005` 산출물을 "있다/없다"가 아니라 실제 파일 경로 기준으로 점검한다.
- 인벤토리 항목:
  - 체크리스트 문서 경로(폰트 파일/라이선스/보관 위치/적용 순서)
  - Firebase 배포 증적 경로(dry-run 또는 실배포 로그)
  - 시각 증적 경로(A/B 시안 캡처 + `AK-005` 매트릭스 캡처)
- 기록 포맷:
  - `artifact_id`: checklist | distribution_log | visual_capture
  - `path`: 실제 파일 경로
  - `status`: ready | missing | revise
  - `owner`: planner | dev | reviewer

## 14. Cycle-26 인벤토리 연동
- 실제 인벤토리 문서: `28-approval-package-inventory.md`
- 운영 원칙:
  - `27`은 규칙/템플릿, `28`은 실제 경로/상태 기록으로 역할 분리
  - `28`의 `missing` 항목이 `ready`로 전환될 때만 `AB-005/AB-006` 착수 승인 검토

## 15. Cycle-27 실행 계획 연동
- missing 해소 실행 계획 문서: `29-missing-artifacts-recovery-plan.md`
- 적용 원칙:
  - `29`의 목표일/담당/경로를 기준으로 `28` 상태를 주기적으로 갱신
  - `AO-005`는 3개 missing 중 최소 1개를 먼저 `ready`로 전환하는 선행 마일스톤

## 16. Cycle-53 UI 품질 게이트 연동
- `S11` 운영 시 `27`은 비주얼 룰북, `40`은 PASS/WARN/FAIL 판정 문서로 역할을 분리한다.
- 운영 규칙:
  - U2 FAIL이면 릴리즈 결론은 최소 `조건부 진행`
  - U2 PASS라도 U5(`S10`) 보류면 최종 결론은 `보류`

## 17. Cycle-54 통합 결론 연동
- `S11` 결과는 `41-unified-quality-verdict-package-spec.md`의 `S12` 규칙(R1~R4)으로 `S10` 결론과 통합한다.

## 18. Cycle-55 드라이런/에스컬레이션 연동
- `S11` 조건부/보류 결과는 `42-unified-verdict-dryrun-and-escalation-spec.md`의 SLA/액션 게이트로 운영한다.

## 19. Cycle-56 이력/추세 연동
- `S11` 결과의 반복 패턴은 `43-unified-verdict-history-and-trend-spec.md`의 H3 규칙으로 클러스터 관리한다.

## 20. Cycle-57 위험예산/프리즈 연동
- UI 품질 관련 조건부/보류 누적 시 `44-unified-verdict-risk-budget-and-freeze-policy-spec.md`의 예산 소진율/프리즈 정책을 따른다.

## 21. Cycle-58 프리즈 지휘/커뮤니케이션 연동
- UI 품질 프리즈 이슈는 `45-freeze-command-and-communication-playbook.md`의 RACI/공지/사후 회고 절차를 따른다.

## 22. Cycle-59 프리즈 드릴/준비도 연동
- UI 품질 프리즈 상황의 실행 준비도는 `46-freeze-drill-readiness-score-spec.md` 점수 기준을 따른다.

## 23. Cycle-60 드릴 보정 액션 폐쇄 루프 연동
- UI 품질 프리즈 이슈 후속 조치의 폐쇄/재개방 운영은 `47-freeze-drill-corrective-action-loop-spec.md` 기준을 따른다.

## 24. Cycle-63 보정 액션 부채/릴리즈 차단 연동
- UI 품질 프리즈 이슈의 부채 관리/릴리즈 차단 판정은 `48-corrective-action-debt-and-release-block-spec.md` 기준을 따른다.

## Cycle-64 보정 액션 부채 이상징후/자동 에스컬레이션 연동
- 보정 액션 부채 이상징후 탐지/경보/응답 SLA 운영은 `49-corrective-action-debt-anomaly-and-escalation-spec.md`를 따른다.

## Cycle-65 에스컬레이션 대응 용량/커버리지 연동
- 에스컬레이션 대응 용량/커버리지 운영은 `50-escalation-capacity-and-coverage-spec.md`를 따른다.
