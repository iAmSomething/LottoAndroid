# ticket_history_csv_effective_range_note_local_2026-03-04

## 목적
- F02 v15: 요청 회차 필터와 실제 CSV 포함 회차가 다를 때 AI 프롬프트에 보정 안내를 추가해 해석 오류를 줄인다.

## 변경 요약
- `SettingsViewModel.buildAiShareText` 확장
  - 요청 필터와 실제 회차 범위를 비교
  - 둘이 다르면 아래 안내 문구 추가:
    - `- 필터 반영: 요청 범위 대비 실제 데이터 포함 회차는 ...`
- 비교 기준
  - 요청 필터: `formatRequestedRange(summary)`
  - 실제 범위: `formatRoundRange(summary)`
  - 둘이 동일하지 않을 때만 안내 문구 노출
- 회귀 테스트 보강 (`SettingsViewModelTest`)
  - 요청 `1200~1205회`, 실제 `1202~1204회` 케이스에서 보정 안내 문구 포함 검증
  - 요청/실제 동일 케이스에서 안내 문구 비포함 검증

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
