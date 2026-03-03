# S05 사용자 여정 리허설 증적

- 실행 시작: 2026-02-26 20:59:27 +0900
- 실행 종료: 2026-02-26 21:00:31 +0900
- 디바이스: `emulator-5554`
- 실행 테스트 클래스: `com.weeklylotto.app.MainNavigationInstrumentedTest,com.weeklylotto.app.WeeklySaveFlowInstrumentedTest,com.weeklylotto.app.QrManualFlowInstrumentedTest,com.weeklylotto.app.SettingsPurchaseRedirectInstrumentedTest,com.weeklylotto.app.ExternalOpenFallbackDialogInstrumentedTest`

## 여정 매핑
- J01 Home -> 결과 조회: `MainNavigationInstrumentedTest`
- J02 Generator -> 저장: `WeeklySaveFlowInstrumentedTest`
- J03 QR 스캔 -> 등록(수동 백업 포함): `QrManualFlowInstrumentedTest`
- J04 공식 구매 이동 경로: `SettingsPurchaseRedirectInstrumentedTest`

## 복구 UX 게이트
- 오류 안내 1초 이내 노출: `SettingsPurchaseRedirectInstrumentedTest`, `ExternalOpenFallbackDialogInstrumentedTest`
- fallback 2탭 이내 도달: `ExternalOpenFallbackDialogInstrumentedTest`

## 판정
- rehearsal coverage: PASS (J01~J04 4개 여정 재현)
- recovery UX gate: PASS (오류 안내 노출 시간/탭 수 회귀 통과)

## 후속 액션
1. J05/J06(프로세스 kill, Wear 핸드오프) 시나리오 계측 추가
2. 실기기에서 동일 세트 1회 재실행 후 비교 리포트 생성
