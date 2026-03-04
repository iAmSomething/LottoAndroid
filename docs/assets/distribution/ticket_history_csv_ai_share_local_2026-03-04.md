# F02 v2 주차별 구매/당첨 CSV 공유 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" --tests "com.weeklylotto.app.SettingsViewModelTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `LocalTicketBackupServiceTest` PASS
  - 주차별 구매 이력 CSV 생성 검증
  - 당첨번호 존재/미존재 회차 매핑 검증(`Y/N`, 당첨번호 컬럼)
- `SettingsViewModelTest` PASS
  - CSV 내보내기 성공 메시지 및 공유 요청 상태 검증
  - CSV 내보내기 실패 메시지 검증
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- `TicketBackupService.exportTicketHistoryCsvForAi` 추가
- `LocalTicketBackupService` CSV 생성 로직 추가
  - 출력 파일: `filesDir/backups/tickets_history_with_draw_latest.csv`
  - 컬럼: 회차/구매일시/게임번호/당첨번호/보너스번호/매칭여부
- Settings 화면 액션 추가: `주차별 구매/당첨 CSV 공유`
- Android `FileProvider` 등록 + `res/xml/file_provider_paths.xml` 추가
