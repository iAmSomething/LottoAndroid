# Ops Observability Check (2026-02-26)

## 실행 목적
- API/로컬 저장 관측성 1차 반영(`ops_api_request`, `ops_storage_mutation`)의 로그 샘플 수집 및 스키마 검증.

## 실행 명령
```bash
./scripts/run-ops-observability-check.sh \
  --serial emulator-5554 \
  --save-log docs/assets/distribution/ops_observability_2026-02-26.log
```

## 실행 결과
- instrumentation: 2/2 PASS
  - `MainNavigationInstrumentedTest`
  - `WeeklySaveFlowInstrumentedTest`
- ops 관측성 검증: PASS
  - profile: `ops-core`
  - 필수 이벤트: `ops_api_request`, `ops_storage_mutation`
  - 필수 스키마 패턴: `component/source/round/latency_ms/status`, `component/operation/status/latency_ms`

## 증적 파일
- 로그 원본: `docs/assets/distribution/ops_observability_2026-02-26.log`
- 검증 스크립트:
  - `scripts/run-ops-observability-check.sh`
  - `scripts/verify-analytics-events.sh --profile ops-core`
