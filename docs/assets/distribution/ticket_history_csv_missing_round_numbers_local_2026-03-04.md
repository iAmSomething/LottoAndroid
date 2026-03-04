# ticket_history_csv_missing_round_numbers_local_2026-03-04

## 목적
- F02 v10: AI 공유 프롬프트에 누락 회차 개수뿐 아니라 누락 회차 번호 목록을 포함해 해석 정확도를 높인다.

## 변경 요약
- `TicketHistoryCsvSummary`에 `missingRoundNumbers: List<Int>` 필드 추가
- `LocalTicketBackupService.exportTicketHistoryCsvForAi`에서 당첨번호 미매칭 회차 번호를 계산해 요약에 반영
- `SettingsViewModel.buildAiShareText`에 `- 누락 회차 번호: ...` 문구 추가
- 회귀 테스트 보강
  - `LocalTicketBackupServiceTest`: 누락 회차 번호 목록 검증
  - `SettingsViewModelTest`: 누락 회차 번호 문구 포함 검증

## 로컬 검증
```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.weeklylotto.app.SettingsViewModelTest" \
  --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" \
  :app:ktlintCheck :app:detekt
```

## 결과
- PASS (`BUILD SUCCESSFUL`)
- 테스트/정적 분석 모두 통과

## 비고
- 실기기 의존 없음(로컬 단위 테스트 및 정적 분석으로 검증 완료)
