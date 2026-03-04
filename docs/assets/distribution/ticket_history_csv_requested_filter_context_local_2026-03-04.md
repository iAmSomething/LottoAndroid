# ticket_history_csv_requested_filter_context_local_2026-03-04

## 목적
- F02 v14: AI 공유 텍스트와 완료 메시지에 `요청 회차 필터`를 명시해 분석 컨텍스트를 강화한다.

## 변경 요약
- `TicketHistoryCsvSummary` 확장
  - `requestedStartRound: Int?`
  - `requestedEndRound: Int?`
- `LocalTicketBackupService`에서 CSV 생성 시 요청 필터 값을 요약 모델에 저장
- `SettingsViewModel` 반영
  - 완료 메시지에 `요청 필터 ...` 문구 추가
  - AI 프롬프트에 `- 요청 필터: ...` 라인 추가
  - 포맷 규칙: `전체`, `n회 이상`, `n회 이하`, `n~m회`
- 테스트 보강
  - `SettingsViewModelTest`: 요청 필터 문구가 메시지/프롬프트에 반영되는지 검증
  - `LocalTicketBackupServiceTest`: 요약 모델에 요청 필터 값이 저장되는지 검증

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
