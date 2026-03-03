# 성능 게이트 리포트

- 실행 시각: 2026-02-26 20:12:40 +0900
- Profile: device
- Device Class: emulator
- Device: Google sdk_gphone64_arm64 (emulator-5554)
- OS/API: Android 16 (API 36)
- Package: `com.weeklylotto.app.debug`
- Runs: total=6, warmup=1, measured=5

## 집계
- Startup median: 2724.00ms
- Startup P95: 5280.00ms
- Jank median: 85.71%
- Jank P95: 100.00%
- ANR count(delta): 0
- Baseline 비교: N/A (device 절대 임계치 사용)

## 판정
- Startup Status: FAIL
- Jank Status: FAIL
- ANR Status: PASS
- Final Verdict: FAIL
- Gate Decision: HOLD
- Release Blocking: YES

## 샘플
| Index | Startup(ms) | Jank(%) | Segment |
|---|---:|---:|---|
| 1 | 4256 | 100.00 | warmup |
| 1 | 5280 | 85.71 | measured |
| 2 | 2559 | 75.00 | measured |
| 3 | 2724 | 80.00 | measured |
| 4 | 2308 | 100.00 | measured |
| 5 | 3128 | 100.00 | measured |

## Notes
- profile=device 실행이지만 serial은 에뮬레이터입니다. 실기기 재검증이 필요합니다.
