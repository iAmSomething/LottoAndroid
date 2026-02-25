# 아키텍처 / 모듈 / 패키지 구조

## 1. 아키텍처 원칙
- 패턴: Compose + MVVM + 단일 앱 모듈
- 계층: `feature(ui/viewmodel)` → `domain(interface/model)` → `data(impl/local/network)`
- DI: Hilt 대신 `AppGraph` 수동 DI (초기 단순화)

## 2. 패키지 구조
- `domain/model`: 핵심 타입
- `domain/repository`: 저장소 계약
- `domain/service`: 유즈케이스 계약
- `data/local`: Room Entity/DAO/DB
- `data/network`: 당첨 API 클라이언트
- `data/qr`: QR 파서
- `data/repository`: 인터페이스 구현체
- `feature/*`: 화면 단위 ViewModel + Screen
- `widget`: Glance 위젯 A/B
- `worker`: 알림 워커

## 3. 상태 관리
- 화면 단위 `StateFlow<UiState>`
- 비즈니스 이벤트는 ViewModel 메서드로 처리
- 저장소 데이터는 Flow로 구독

## 4. 데이터 흐름
1. 사용자 액션 → ViewModel
2. ViewModel → domain service/repository 호출
3. repository는 local/network를 조합
4. 결과를 UiState로 반영

## 5. 확장 포인트
- 멀티모듈 전환 시 `domain`/`data`를 분리 모듈로 이동
- QR 포맷 추가 시 `QrTicketParser` 전략 분리
- draw API 변경 시 `DrawApiClient`만 교체
