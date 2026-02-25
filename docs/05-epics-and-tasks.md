# 에픽 / 태스크 백로그

## Epic A. 프로젝트 기반
- A-1 Gradle/Compose/Room/WorkManager/Glance 구성
- A-2 공통 Result/Error 타입 및 패키지 구조 생성
- A-3 CI/정적분석 구성
- DoD: 디버그 빌드 + 단위테스트 + 문서 01/02 완료

## Epic B. 번호 생성/관리
- B-1 5게임 자동 생성
- B-2 번호 잠금 토글 + 잠금 제외 재생성
- B-3 수동 입력/반자동 로직
- B-4 이번 주 번호 저장
- DoD: `design/lotto_generator.svg` 동작 반영

## Epic C. 구매 등록
- C-1 QR 스캔 화면 및 권한 처리
- C-2 QR URL 파싱 + 회차 그룹화 저장
- C-3 홈 구매번호 카드 연동
- DoD: `design/lotto_home.svg`의 구매 카드 데이터 연동

## Epic D. 결과 연동/자동 채점
- D-1 당첨 API 연동 + 캐시 fallback
- D-2 등수 판정 로직(1~5등/낙첨)
- D-3 결과 화면 하이라이트
- DoD: `design/lotto_result.svg` 수준 상태표시 구현

## Epic E. 알림
- E-1 기본 주간 알림 예약
- E-2 커스텀 설정/재예약
- E-3 앱 재시작 이후 예약 보존 검증
- DoD: 예약/취소/재예약 테스트 통과

## Epic F. 위젯
- F-1 타입 A(이번 주 번호)
- F-2 타입 B(결과 요약)
- F-3 데이터 변경 시 위젯 갱신
- DoD: `design/lotto_widgets.svg` 정보 우선순위 일치

## Epic G. 통계
- G-1 구매/당첨 누적 집계
- G-2 TOP 번호 집계
- DoD: 계산 테스트 + 화면 일관성 확보

## Epic H. 품질/릴리즈
- H-1 단위/통합/UI 테스트 보강
- H-2 접근성/성능 점검
- H-3 릴리즈 문서/체크리스트 확정
- DoD: 문서 06/07 + 테스트 리포트 완료
