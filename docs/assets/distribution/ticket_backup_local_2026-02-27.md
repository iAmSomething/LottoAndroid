# F02 v1 로컬 백업/복원 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.LocalTicketBackupServiceTest" --tests "com.weeklylotto.app.SettingsViewModelTest"
```

## 결과
- `LocalTicketBackupServiceTest` PASS
  - 백업 파일 생성 확인
  - 백업 복원 시 기존 티켓 교체 확인
  - 백업 파일 부재 시 실패 처리 확인
- `SettingsViewModelTest` PASS
  - 백업 성공 메시지 노출 확인
  - 복원 실패 메시지 노출 확인

## 반영 범위
- Settings 화면에 `백업 파일 생성`, `최근 백업 복원` 액션 추가
- 백업 파일 경로: `filesDir/backups/tickets_backup_latest.json`
