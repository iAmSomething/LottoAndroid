# ticket_history_csv_reliability_grade_local_2026-03-04

## 목적
- F02 v17: CSV/AI 공유 텍스트에 `분석 신뢰 등급(높음/보통/낮음)`을 추가해 데이터 신뢰도를 빠르게 판단할 수 있게 한다.

## 변경 요약
- `SettingsViewModel`에 신뢰 등급 계산 로직 추가
  - 입력 지표: 당첨번호 커버리지(%), 필터 충족률(요청 필터가 있을 때)
  - 등급 기준
    - 높음: 커버리지 >= 90 && 충족률 >= 80
    - 보통: 커버리지 >= 60 && 충족률 >= 50
    - 낮음: 그 외
- CSV 완료 메시지에 `신뢰등급 ...` 추가
- AI 프롬프트에 `- 분석 신뢰 등급: ...` 추가
- 회귀 테스트 보강 (`SettingsViewModelTest`)
  - 전체 필터: `높음` + 충족률 문구 미표시
  - 100% 충족 + 커버리지 66%: `보통`
  - 부분 충족(2/6) + 커버리지 50%: `낮음`

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
