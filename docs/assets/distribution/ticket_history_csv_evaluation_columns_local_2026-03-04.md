# F02 v3 CSV 당첨 평가 컬럼 확장 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" --tests "com.weeklylotto.app.SettingsViewModelTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `LocalTicketBackupServiceTest` PASS
  - CSV 헤더에 평가 컬럼 포함 검증
  - 당첨 회차의 게임별 평가값(`matched_main_count`, `bonus_matched`, `draw_rank`, `expected_prize_amount`) 검증
  - 당첨정보 미존재 회차의 평가 컬럼 빈값 처리 검증
- `SettingsViewModelTest` PASS
  - CSV 생성 성공/실패 메시지 및 공유 요청 상태 검증
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- CSV 컬럼 확장:
  - `matched_main_count`
  - `bonus_matched`
  - `draw_rank`
  - `expected_prize_amount`
- Settings CSV 공유 CTA 계측 이벤트 추가:
  - `interaction_cta_press`
  - `screen=settings`, `component=ticket_history_csv_share`, `action=click`
