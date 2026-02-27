# Stats Source/ROI Enhancement Check (2026-02-27)

## 실행 목적
- `C03/C04` v2 반영(출처별 ROI%/회차별 ROI%)의 계산 및 회귀 테스트 통과 확인.

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
  - 출처별 ROI% 계산(`generated=400`, `manual=-100`, `qr=-100`)
  - 회차별 ROI 트렌드 ROI% 계산(테스트 시나리오 기준 `-100%`)

## 관련 파일
- `app/src/main/java/com/weeklylotto/app/feature/stats/StatsViewModel.kt`
- `app/src/main/java/com/weeklylotto/app/feature/stats/StatsScreen.kt`
- `app/src/test/java/com/weeklylotto/app/StatsViewModelTest.kt`
