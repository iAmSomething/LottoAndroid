# A06 v1 외부 이동 안내 모달 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.feature.common.PurchaseRedirectNoticeTest" --tests "com.weeklylotto.app.feature.home.HomeExternalLinkTest"
```

## 결과
- `PurchaseRedirectNoticeTest` PASS
  - 평일 낮 `OPEN` 상태 확인
  - 토요일 19시대 `CLOSING_SOON` 상태 확인
  - 토요일 20시 이후/새벽 시간 `CLOSED` 상태 확인
- `HomeExternalLinkTest` PASS
  - 판매점 검색 URL 생성 로직 회귀 이상 없음

## 반영 범위
- `buildPurchaseRedirectNotice` 공통 정책 함수 추가
- Home/Result/Settings의 외부 이동 1회 안내 모달을 정책 기반 동적 문구로 통일
- 안내 상태에 따른 경고 톤 색상 반영(`OPEN`/`CLOSING_SOON`/`CLOSED`)
