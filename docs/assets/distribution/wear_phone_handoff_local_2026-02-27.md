# D02 v1 워치→폰 핸드오프 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.AppDeepLinkTest" :wear:assembleDebug
```

## 결과
- `AppDeepLinkTest` PASS
  - `weeklylotto://open?route=...` 포맷 라우팅 확인
  - legacy `weeklylotto://qr_scan` 호환 라우팅 확인
  - 비지원 route/scheme 차단 확인
- `:wear:assembleDebug` SUCCESS
  - 워치 핸드오프 런처가 `open?route` 계약으로 빌드됨

## 반영 범위
- 앱 딥링크 파서 강화: `deepLinkToRouteString`/`routeToDeepLinkString` 추가
- 워치 핸드오프 URI 생성 규칙 통일: `weeklylotto://open?route=<target>&source=wear`
- 워치 핸드오프 허용 라우트 화이트리스트 적용(QR/결과/설정)
