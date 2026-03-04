# F02 v8 AI 프롬프트 데이터 신뢰도/회차 범위 문구 점검 리포트 (2026-03-04)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.SettingsViewModelTest" --tests "com.weeklylotto.app.LocalTicketBackupServiceTest"
./gradlew :app:ktlintCheck :app:detekt
```

## 결과
- `SettingsViewModelTest` PASS
  - 매칭 회차 비율 기반 데이터 신뢰도 문구 포함 검증
  - 프롬프트 `회차 범위` 문구 포함 검증(`1200~1201회`, `1201~1203회`)
  - 누락 회차 존재 시 경고 + 해석 주의 문구 동시 포함 검증
- `LocalTicketBackupServiceTest` PASS
  - CSV 생성/요약 계산 회귀 없음 확인
- `ktlintCheck` PASS
- `detekt` PASS

## 반영 범위
- AI 프롬프트 문구 확장:
  - `- 데이터 신뢰도: XX% (당첨번호 매칭 회차 기준)`
  - `- 회차 범위: XXXX~YYYY회`
  - 누락 회차 존재 시 `경고` + `해석 주의` 문구 추가
