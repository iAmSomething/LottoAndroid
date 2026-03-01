# B03 v1 주간 리포트 카드 보강 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.HomeViewModelTest"
```

## 결과
- `HomeViewModelTest` PASS
  - 주간 리포트의 당첨률(`winningRatePercent`) 계산 검증
  - 주간 리포트의 결과 확인 상태(`resultViewed`) 계산 검증
  - 최신 회차 미확인/확인 상태별 리포트 상태 회귀 검증

## 반영 범위
- `WeeklyReportSummary`에 `winningRatePercent`/`resultViewed` 지표 추가
- Home 주간 리포트 카드에 `당첨률` + `결과 확인 상태` 노출 추가
- 주간 리포트 계산 회귀 테스트 1건 추가(`HomeViewModelTest`)
