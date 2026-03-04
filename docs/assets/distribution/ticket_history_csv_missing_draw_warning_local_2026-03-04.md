# F02 v6 AI 프롬프트 누락 회차 경고 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest" --tests "com.weeklylotto.app.LocalTicketBackupServiceTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `SettingsViewModelTest` PASS
  - 누락 회차(`missingDrawCount > 0`) 시 프롬프트에 경고 문구 포함 검증
- `LocalTicketBackupServiceTest` PASS
  - CSV 생성/요약 계산 회귀 없음 확인
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- AI 공유 프롬프트 확장:
  - 누락 회차가 있으면 `- 경고: 당첨번호가 없는 회차 N개 포함` 문구 자동 추가
- Settings CSV 공유 인텐트에서 경고 포함 프롬프트 전달
