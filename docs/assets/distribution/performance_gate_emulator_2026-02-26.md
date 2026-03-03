# 성능 게이트 리포트

- 실행 시각: 2026-02-26 20:45:49 +0900
- Profile: emulator
- Device Class: emulator
- Device: Google sdk_gphone64_arm64 (emulator-5554)
- OS/API: Android 16 (API 36)
- Package: `com.weeklylotto.app.debug`
- Runs: total=6, warmup=1, measured=5

## 집계
- Startup median: 1031.00ms
- Startup P95: 1082.00ms
- Jank median: 33.33%
- Jank P95: 43.75%
- ANR count(delta): 0
- Baseline source: performance_sample_2026-02-26.md
- Baseline Startup P95: 3772ms
- Baseline Jank P95: 50.00%
- Startup degradation: -71.31%
- Jank degradation: -12.50%

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
| 1 | 3115 | 23.81 | warmup |
| 1 | 1038 | 33.33 | measured |
| 2 | 1031 | 33.33 | measured |
| 3 | 910 | 35.29 | measured |
| 4 | 964 | 43.75 | measured |
| 5 | 1082 | 29.41 | measured |

## Notes
