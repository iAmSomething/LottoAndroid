# 다음 스프린트 백로그

## 1. 목표
- MVP 이후 고도화 항목을 우선순위 기반으로 실행한다.
- 기능 출시보다 KPI 개선(리텐션/루틴 완료율/안정성)을 우선한다.

## 2. 우선순위 High
- 비주얼/타이포 리프레시 2차 적용(Generator/Manage 확장 + 상호작용 enum 정렬) (`T01`, `R-009`~`R-012`)
- Wear OS v1 실제 화면 구현(placeholder → Home/Numbers/Result/Settings 4화면) (`D01`, `K-001`~`K-012`) - 완료(2026-02-26, `wear/src/main/java/com/weeklylotto/wear/WearApp.kt`)
- 워치→폰 핸드오프 최소 경로 구현(`D02`, `K-009`~`K-012`) - 완료(2026-02-26, `wear-remote-interactions` + 딥링크 핸드오프)
- 핵심 상호작용 모션 2차 코드 적용(`G03`, `INT-01`~`INT-05`) - 1차 완료(2026-02-26, `LottoBottomBar`/`BallChip` animated feedback + Reduce Motion 연동)
- Reduce Motion 실제 설정/동작 반영(`G06`, `M-012`) - 완료(2026-02-26, Settings 토글 + DataStore + Splash/컴포넌트 반영)
- EXP-05/06 계측 이벤트 실제 훅 연결(`M-016`~`M-018`)
- 기획/구현 상태 분리 리포팅 루틴 고정(`P-001`)

## 3. 우선순위 Medium
- 통계 인사이트 확장(출처별 성과/ROI 트렌드/중복도 경고) (`C03`, `C04`, `C01`)
- API/로컬 데이터 관측성 스펙 고도화 (`E01`, `L-010`, `L-011`)
- 릴리즈 위험 점수 모델 운영 반영(`L-012`)
- 번호관리 빠른 액션 후속 최적화(`A02`)

## 4. 우선순위 Low
- Wear 컴플리케이션/고급 알림 액션(`D03`, `D04`)
- 프리미엄 후보 기능 검토(`F01`, `F02`)
- 파트너십/위치 연계 후보 검토(`F04`)

## 5. 완료 기준(DoD)
- High 항목 중 최소 2개는 실제 코드 반영 + 테스트 증적 확보
- 타이포 리프레시 적용 전/후 스크린샷 비교 1세트(Home, Result) 확보
- Wear 4화면 중 최소 2화면은 워치 에뮬레이터 스크린샷/로그 증적 확보
- 모션 항목(SPL/INT) 중 최소 1개는 `Animated*` 계열 적용 증적 확보
- `10-detailed-todo-board.md`에서 High 항목 `50% 이상`이 `[~]` 또는 `[x]`
- `22-prioritization-matrix.md` 점수 재평가 1회 완료
- `11-progress-tracker.md`에 실행 로그 기록 완료
