# B02 v1 미확인 결과 리마인드 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.ResultReminderPolicyTest" --tests "com.weeklylotto.app.PurchaseReminderPolicyTest"
```

## 결과
- `ResultReminderPolicyTest` PASS
  - 최신 회차 티켓 보유 + 미확인 상태에서 미확인 결과 리마인드 발송 검증
  - 최신 회차 티켓 미보유 시 알림 스킵 검증
  - 최신 회차 이미 확인 시 알림 스킵 검증
- `PurchaseReminderPolicyTest` PASS (기존 B04 정책 회귀)

## 반영 범위
- `ResultReminderWorker`에 미확인 결과 기준 발송/스킵 정책 연동
- `resolveResultReminderMessage` 정책 추가
  - 최신 회차 티켓 보유 + 미확인: 리마인드 발송
  - 티켓 미보유 또는 이미 확인: 알림 스킵
  - 최신 회차 조회 실패: 기본 결과 알림 fallback
