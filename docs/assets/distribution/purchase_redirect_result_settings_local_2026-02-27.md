# A05 v2 구매 리다이렉트 확장 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.feature.home.HomeExternalLinkTest"
```

## 결과
- `HomeExternalLinkTest` PASS
  - 지도 검색 URL 빌더 회귀 통과
- Result/Settings 확장 컴파일 확인
  - `ResultScreen`, `SettingsScreen`에 구매 CTA + 안내/실패 fallback 반영

## 반영 범위
- 공통 외부 링크 유틸: `ExternalLinkSupport.kt`
- 구매 CTA 확장: Result/Settings
- 공통 정책 유지: 1회 안내 모달 + 실패 시 `브라우저로 열기`/`링크 복사`
