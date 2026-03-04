# ticket_history_csv_filter_fulfillment_rate_local_2026-03-04

## 목적
- F02 v16: 요청 회차 필터가 있을 때 실제 포함 회차 비율을 `필터 충족률`로 명시해 데이터 충분성을 빠르게 판단할 수 있게 한다.

## 변경 요약
- `SettingsViewModel` 반영
  - CSV 완료 메시지에 `필터 충족률 x/y회차(z%)` 조건부 추가
  - AI 프롬프트에 `- 필터 충족률 x/y회차(z%)` 조건부 추가
- 계산 규칙
  - 요청 시작/끝 회차가 모두 있는 경우만 계산
  - `requestedCount = end - start + 1`
  - `fulfilledCount = summary.roundCount`
  - `percent = fulfilledCount * 100 / requestedCount`
- 회귀 테스트 보강 (`SettingsViewModelTest`)
  - 전체 필터(미출력)
  - 100% 충족(3/3)
  - 부분 충족(2/6, 33%)

## 로컬 검증
```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.weeklylotto.app.SettingsViewModelTest" \
  --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" \
  :app:ktlintCheck :app:detekt
```

## 결과
- PASS (`BUILD SUCCESSFUL`)

## 비고
- 실기기 의존 없음
