# ticket_history_csv_source_breakdown_prompt_local_2026-03-04

## 목적
- F02 v11: AI 분석 프롬프트에 출처별 게임 수(자동/수동/QR)를 포함해 출처 편향을 바로 해석할 수 있게 한다.

## 변경 요약
- `TicketHistoryCsvSummary` 확장
  - `generatedGameCount`
  - `manualGameCount`
  - `qrGameCount`
- `LocalTicketBackupService.exportTicketHistoryCsvForAi`에서 티켓 출처별 게임 수 집계 추가
- `SettingsViewModel.buildAiShareText`에 아래 문구 추가
  - `- 출처별 게임 수: 자동 x, 수동 y, QR z`
- 회귀 테스트 보강
  - `LocalTicketBackupServiceTest`: 혼합 출처 티켓 케이스에서 출처별 게임 수 집계 검증
  - `SettingsViewModelTest`: 프롬프트 문구 포함 검증

## 로컬 검증
```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.weeklylotto.app.SettingsViewModelTest" \
  --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" \
  :app:ktlintCheck :app:detekt
```

## 결과
- PASS (`BUILD SUCCESSFUL`)
- 테스트/정적 분석 통과

## 비고
- 실기기 의존 없음(로컬 단위 테스트와 정적 분석으로 확인)
