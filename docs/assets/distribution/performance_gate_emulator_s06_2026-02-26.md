# 성능 게이트 리포트

- 실행 시각: 2026-02-26 21:33:04 +0900
- Profile: emulator
- Device Class: emulator
- Device: Google sdk_gphone64_arm64 (emulator-5554)
- OS/API: Android 16 (API 36)
- Package: `com.weeklylotto.app.debug`
- Runs: total=6, warmup=1, measured=5

## 집계
- Startup median: 1043.00ms
- Startup P95: 1135.00ms
- Jank median: 36.84%
- Jank P95: 42.11%
- ANR count(delta): 0
- Baseline source: performance_sample_2026-02-26.md
- Baseline Startup P95: 3772ms
- Baseline Jank P95: 50.00%
- Startup degradation: -69.91%
- Jank degradation: -15.78%

## 판정
- Startup Status: PASS
- Jank Status: PASS
- ANR Status: PASS
- Final Verdict: PASS
- Gate Decision: PROCEED
- Release Blocking: NO

## 샘플
| Index | Startup(ms) | Jank(%) | Segment |
|---|---:|---:|---|
| 1 | 2466 | 31.58 | warmup |
| 1 | 1135 | 35.00 | measured |
| 2 | 1003 | 31.58 | measured |
| 3 | 986 | 38.89 | measured |
| 4 | 1043 | 42.11 | measured |
| 5 | 1129 | 36.84 | measured |

## Notes
