# F02 v9 AI 프롬프트 CSV 스키마 가이드 문구 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest" --tests "com.weeklylotto.app.LocalTicketBackupServiceTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `SettingsViewModelTest` PASS
  - AI 프롬프트 `CSV 스키마 가이드` 섹션 포함 검증
  - `draw_rank/expected_prize_amount` 설명 문구 포함 검증
- `LocalTicketBackupServiceTest` PASS
  - CSV 생성/요약 지표 계산 회귀 없음 확인
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- AI 프롬프트 문구 확장:
  - `CSV 스키마 가이드` 섹션 추가
  - `game_numbers`, `draw_main_numbers/draw_bonus_number`, `draw_rank/expected_prize_amount` 컬럼 설명 포함
