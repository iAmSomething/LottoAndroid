# B01 v1 개인 루틴 캘린더(최근 8주) 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.HomeViewModelTest" --tests "com.weeklylotto.app.ResultViewModelTest"
```

## 결과
- `HomeViewModelTest` PASS
  - 최근 8주 루틴 히스토리(`routineHistory`) 생성 검증
  - 회차별 구매 게임 수/결과 확인 여부 계산 검증
- `ResultViewModelTest` PASS
  - `ResultViewTracker` 인터페이스 확장(`loadRecentViewedRounds`) 반영 후 기존 조회/기록 흐름 회귀 통과

## 반영 범위
- `ResultViewTracker`에 최근 확인 회차 조회 API 추가
- `DataStoreResultViewTracker`에 최근 확인 회차 히스토리 저장/복원 로직 추가
- `HomeViewModel`에 `routineHistory`(최근 8주 구매/확인 히스토리) 계산 추가
- Home 화면에 `최근 8주 루틴 히스토리` 카드 추가
