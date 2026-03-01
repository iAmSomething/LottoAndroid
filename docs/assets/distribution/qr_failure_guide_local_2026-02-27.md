# A04 v1 QR 실패 유형 가이드 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.QrTicketParserTest" --tests "com.weeklylotto.app.QrScanViewModelTest"
```

## 결과
- `QrTicketParserTest` PASS
  - 지원하지 않는 형식 실패 코드(`[qr:unsupported_format]`) 확인
  - payload 누락 실패 코드(`[qr:missing_payload]`) 확인
  - 회차 형식 오류 실패 코드(`[qr:invalid_round]`) 확인
- `QrScanViewModelTest` PASS
  - 실패 코드별 가이드 타이틀/메시지 매핑 확인
  - 번호 형식 오류(중복 번호) 가이드 확인
  - 성공 파싱 시 실패 가이드 초기화 확인

## 반영 범위
- `QrTicketParser`에 실패 코드 분류 추가
- `QrScanViewModel`에 실패 유형별 가이드 타이틀/메시지 매핑 추가
- `QrScanScreen` 실패 카드에 가이드 타이틀 노출 추가
