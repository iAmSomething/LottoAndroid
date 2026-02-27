# Stats Number Distribution Heatmap Check (2026-02-27)

## 실행 목적
- `C02` 번호 분포 히트맵(v1) 집계 로직 및 회귀 테스트 통과 확인.

## 실행 명령
```bash
./gradlew :app:testDebugUnitTest \
  --tests "com.weeklylotto.app.StatsViewModelTest" \
  -Dkotlin.incremental=false \
  -Dkotlin.compiler.execution.strategy=in-process
```

## 실행 결과
- `StatsViewModelTest` PASS
- 검증 포인트
  - 구간 분포 집계(1-9/10-19/20-29/30-39/40-45) count/percent 계산
  - 데이터 없음 케이스에서 기본 5개 구간(0값) 유지

## 관련 파일
- `app/src/main/java/com/weeklylotto/app/feature/stats/StatsViewModel.kt`
- `app/src/main/java/com/weeklylotto/app/feature/stats/StatsScreen.kt`
- `app/src/test/java/com/weeklylotto/app/StatsViewModelTest.kt`
