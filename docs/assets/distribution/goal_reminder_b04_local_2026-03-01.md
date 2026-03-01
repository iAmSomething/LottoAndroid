# B04 v1 목표 기반 알림 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.PurchaseReminderPolicyTest"
```

## 결과
- `PurchaseReminderPolicyTest` PASS
  - 토요일 18시 이전 + 미등록 시 목표형 알림 문구 선택 검증
  - 토요일 18시 이전 + 등록 완료 시 알림 스킵 검증
  - 토요일 18시 이후 기본 구매 알림 문구 fallback 검증

## 반영 범위
- `PurchaseReminderWorker`에 이번 주 번호 등록 여부 기반 목표형 알림 정책 연동
- `resolvePurchaseReminderMessage` 정책 추가
  - 토요일 18시 이전 미등록: 목표형 리마인드 발송
  - 토요일 18시 이전 등록 완료: 알림 스킵(피로도 완화)
  - 그 외 시간: 기존 구매 알림 유지
