# ticket_history_csv_round_range_filter_local_2026-03-04

## 목적
- F02 v12: 주차별 구매/당첨 CSV 공유 시 회차 범위(시작/끝)를 지정해 필요한 구간만 내보낼 수 있도록 한다.

## 변경 요약
- `TicketBackupService.exportTicketHistoryCsvForAi(startRound, endRound)` 시그니처 확장
- `LocalTicketBackupService`에 회차 범위 필터 적용(집계/CSV 생성 모두 필터 기준)
- `SettingsViewModel`에 회차 범위 역전 검증 추가(`startRound > endRound` 차단)
- `SettingsScreen`에 시작/끝 회차 입력 UI 추가 + 숫자 입력 제한 + 검증 메시지 처리
- 테스트 보강
  - `SettingsViewModelTest`: 필터 파라미터 전달/역전 범위 차단 검증
  - `LocalTicketBackupServiceTest`: 회차 필터 적용 CSV 결과 검증

## 로컬 검증
```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.weeklylotto.app.SettingsViewModelTest" \
  --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" \
  :app:ktlintCheck :app:detekt
```

## 결과
- PASS (`BUILD SUCCESSFUL`)
- 단위 테스트/정적 분석 통과

## 비고
- 실기기 의존 없음(로컬 검증 완료)
