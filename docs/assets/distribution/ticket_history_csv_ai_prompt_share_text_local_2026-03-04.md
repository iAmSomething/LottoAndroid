# F02 v5 AI 프롬프트 공유 텍스트 확장 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest" --tests "com.weeklylotto.app.LocalTicketBackupServiceTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `SettingsViewModelTest` PASS
  - CSV 생성 성공 시 `csvShareRequest.shareText`에 AI 분석 프롬프트 포함 검증
  - 요약 지표(당첨 게임 수) 기반 동적 문구 포함 검증
- `LocalTicketBackupServiceTest` PASS
  - CSV 생성/요약 계산 회귀 없음 확인
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- `CsvShareRequest`에 `shareText` 필드 추가
- Settings CSV 공유 인텐트의 `Intent.EXTRA_TEXT`를 동적 프롬프트 텍스트로 교체
- 프롬프트 텍스트 구성:
  - 회차/티켓/게임/당첨 회차 요약
  - 당첨 게임 수/예상당첨금 합계
  - 분석 요청 3개 항목(패턴/출처별 성과/다음 주차 전략)
