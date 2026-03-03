# Hotpath S06 Profile (2026-02-26)

## 실행 목적
- `S06` 핫패스(Home/Result/Manage) 구간의 startup/render/jank 기준 충족 여부를 점검한다.

## 실행 명령
```bash
GRADLE_USER_HOME=/Volumes/무제/lotto/.gradle-user-home ANDROID_SERIAL=emulator-5554 \
  ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.weeklylotto.app.HotpathRenderLatencyInstrumentedTest

GRADLE_USER_HOME=/Volumes/무제/lotto/.gradle-user-home \
  ./scripts/run-performance-sample-check.sh \
  --serial emulator-5554 \
  --profile emulator \
  --repeat 5 \
  --warmup 1 \
  --baseline-report docs/assets/distribution/performance_sample_2026-02-26.md \
  --save-report docs/assets/distribution/performance_gate_emulator_s06_2026-02-26.md
```

## 측정 결과
| Screen | Scenario | startup | render | jank | verdict | notes |
|---|---|---:|---:|---:|---|---|
| Home | cold start + first render | P95 1135ms | <=1000ms(assert) | P95 42.11%(emulator profile) | PASS | `HotpathRenderLatencyInstrumentedTest`, `performance_gate_emulator_s06_2026-02-26.md` |
| Result | bottom tab -> result render | n/a | <=1000ms(assert) | P95 42.11%(shared) | PASS | `HotpathRenderLatencyInstrumentedTest` |
| Manage | bottom tab -> manage render | n/a | <=1000ms(assert) | P95 42.11%(shared) | PASS | `HotpathRenderLatencyInstrumentedTest` |

## 판정 근거
1. Home startup P95는 1135ms로 `S06` P0 목표(2.2s 이하)를 충족.
2. Home/Result/Manage 렌더는 핫패스 렌더 지연 테스트(`HotpathRenderLatencyInstrumentedTest`)에서 3/3 PASS.
3. jank는 emulator 운영 규칙(`S08` baseline-degradation 방식) 기준 PASS이며, baseline 대비 -15.78% 개선.

## 최종 판정
- `S06 verdict`: PASS (emulator profile 기준)

## 연결 증적
- `docs/assets/distribution/performance_gate_emulator_s06_2026-02-26.md`
- `app/src/androidTest/java/com/weeklylotto/app/HotpathRenderLatencyInstrumentedTest.kt`
