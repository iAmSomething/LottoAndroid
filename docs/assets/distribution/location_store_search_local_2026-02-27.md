# Location Store Search Local Verification (F04 v1)

- Date: 2026-02-27
- Scope: Home `근처 판매점 찾기` CTA + URL 빌더 회귀 테스트

## Executed Command

```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.feature.home.HomeExternalLinkTest"
```

## Result

- Status: PASS
- Notes:
  - `buildLottoStoreSearchUrl` 기본 질의(`로또 판매점`) URL 인코딩 검증 통과
  - 커스텀 질의 URL 인코딩 검증 통과
