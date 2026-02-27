# E02 v1 로컬 백업 무결성 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" --tests "com.weeklylotto.app.SettingsViewModelTest"
```

## 결과
- `LocalTicketBackupServiceTest` PASS
  - 정상 백업 무결성 점검 시 문제 없음 집계 확인
  - 중복/게임오류/깨진 레코드 집계 및 `ops_data_integrity` warn 로깅 확인
- `SettingsViewModelTest` PASS
  - 무결성 점검 문제 없음 메시지 확인
  - 무결성 점검 문제 요약 메시지 확인

## 반영 범위
- `TicketBackupService.verifyLatestBackupIntegrity` 인터페이스 추가
- `LocalTicketBackupService`에 백업 JSON 무결성 점검 로직 추가
- Settings 화면에 `백업 무결성 점검` 액션 추가
- 계측 이벤트: `ops_data_integrity` (`screen=settings`, `component=backup_integrity`)
