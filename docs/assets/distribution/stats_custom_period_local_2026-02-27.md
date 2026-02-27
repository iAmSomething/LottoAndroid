# Stats Custom Period Local Verification (F01 v1)

- Date: 2026-02-27
- Scope: Stats custom round-range filter (`직접 입력`) + validation behavior

## Executed Command

```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.StatsViewModelTest"
```

## Result

- Status: PASS
- Added regression checks:
  - `커스텀_회차_필터는_지정_범위만_포함한다`
  - `커스텀_회차_필터는_잘못된_범위면_오류를_표시한다`
