# Analytics Sample Check (2026-02-26)

## 실행 목적
- 통계 중복도 경고 카드 CTA(`duplicate_warning_card`) 클릭 계측이 실제 로그에 기록되는지 검증.

## 실행 명령
```bash
./scripts/run-analytics-sample-check.sh \
  --serial emulator-5554 \
  --save-log docs/assets/distribution/analytics_sample_2026-02-26.log
```

## 실행 결과
- instrumentation: 3/3 PASS
  - `HomeScreenSmokeTest`
  - `WeeklySaveFlowInstrumentedTest`
  - `StatsCtaInstrumentedTest`
- analytics 검증: PASS
  - profile: `stats-cta`
  - 필수 이벤트: `motion_splash_shown`, `motion_splash_skip`, `interaction_cta_press`
  - 필수 스키마 패턴: `screen=stats`, `component=duplicate_warning_card`, `action=click`

## 증적 파일
- 로그 원본: `docs/assets/distribution/analytics_sample_2026-02-26.log`
- 검증 스크립트:
  - `scripts/run-analytics-sample-check.sh`
  - `scripts/verify-analytics-events.sh`
