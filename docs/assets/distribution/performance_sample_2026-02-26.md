# 성능 샘플 게이트 리포트

- 실행 시각: 2026-02-26 19:48:48 +0900
- 디바이스: `emulator-5554`
- 패키지: `com.weeklylotto.app.debug`

## 측정 결과
- Startup TotalTime: 3772ms (기준 <= 2200ms) => FAIL
- Janky frames: 50.00% (기준 <= 3.0%) => FAIL
- ANR count(exit-info): 0 (기준 <= 0) => PASS

## 게이트 판정
- FAIL

## Raw am start -W
```
Starting: Intent { cmp=com.weeklylotto.app.debug/com.weeklylotto.app.MainActivity }
Status: ok
LaunchState: COLD
Activity: com.weeklylotto.app.debug/com.weeklylotto.app.MainActivity
TotalTime: 3772
WaitTime: 3781
Complete
```
