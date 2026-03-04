# ticket_history_csv_empty_range_guard_local_2026-03-04

## 목적
- F02 v13: CSV 회차 필터 범위에 데이터가 없을 때 공유 플로우를 시작하지 않고 사용자에게 명확히 안내한다.

## 변경 요약
- `SettingsViewModel.exportTicketHistoryCsvForAi`
  - CSV 생성 결과 `ticketCount == 0`이면 `csvShareRequest`를 만들지 않고
  - 메시지 `선택한 회차 범위에 내보낼 데이터가 없습니다.` 노출
- 회귀 테스트 추가
  - `SettingsViewModelTest`: 무데이터 범위에서 공유 차단 + 메시지 검증
  - `LocalTicketBackupServiceTest`: 회차 필터 결과 0건일 때 빈 요약/헤더-only CSV 검증

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
