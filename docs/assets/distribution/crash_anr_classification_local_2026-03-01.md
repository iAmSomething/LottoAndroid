# Crash/ANR Auto Classification Report

- Generated at (UTC): 2026-03-01T05:49:29Z
- Source: log_file:/tmp/crash_anr_sample_2026-03-01.log
- Candidate lines detected: 8
- Classified lines used: 8 (max 200)

## Summary

| Category | Count | Severity | Owner | Immediate Action |
|---|---:|---|---|---|
| ANR / Main Thread Block | 1 | P0 | app-platform | 메인스레드 long task 추적 + blocking call 제거 |
| ANR / Startup | 1 | P0 | app-platform | App start path profile + cold start trace 확인 |
| Crash / Camera | 1 | P1 | qr-owner | camera lifecycle/permission race 재현 점검 |
| Crash / External Redirect | 1 | P2 | navigation-owner | intent 해상도/fallback(copy/open browser) 점검 |
| Crash / Network | 1 | P2 | api-owner | timeout/offline fallback 경로 및 retry 정책 점검 |
| Crash / Resource Pressure | 1 | P0 | app-platform | OOM/disk pressure 재현 + 객체/파일 점유량 점검 |
| Crash / Runtime | 1 | P0 | feature-owner | stacktrace root-cause + null/state guard 추가 |
| Crash / Storage | 1 | P1 | data-owner | DB/파일 I/O 예외 복구 경로 점검 |

## Representative Signals

- **ANR / Main Thread Block** (1)
  - sample: `ANR in com.weeklylotto.app (com.weeklylotto.app/.MainActivity) Input dispatching timed out`
- **ANR / Startup** (1)
  - sample: `ANR in com.weeklylotto.app startup Application.onCreate blocked by AppGraph.init`
- **Crash / Camera** (1)
  - sample: `CameraX ImageAnalysis bind failed java.lang.IllegalStateException: LifecycleOwner is destroyed`
- **Crash / External Redirect** (1)
  - sample: `java.lang.ActivityNotFoundException: No Activity found to handle Intent { act=android.intent.action.VIEW dat=https://dhlottery.co.kr }`
- **Crash / Network** (1)
  - sample: `java.net.SocketTimeoutException: timeout while calling draw endpoint`
- **Crash / Resource Pressure** (1)
  - sample: `java.lang.OutOfMemoryError: Failed to allocate a 2097168 byte allocation`
- **Crash / Runtime** (1)
  - sample: `FATAL EXCEPTION: main java.lang.IllegalStateException: NavController graph has not been set`
- **Crash / Storage** (1)
  - sample: `android.database.sqlite.SQLiteDiskIOException: disk I/O error`

## Triage Template

- 재현 여부: [ ] 재현됨 / [ ] 미재현
- 영향 범위: [ ] startup [ ] result [ ] generator [ ] manage [ ] settings [ ] wear
- 임시 완화책: [ ] feature flag [ ] retry [ ] fallback UX [ ] 안내문구
- 다음 액션: owner 할당 + ETA + 회귀 테스트 항목 정의
