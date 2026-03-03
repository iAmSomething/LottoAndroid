# Physical Device Readiness Checklist (2026-02-27)

## 목적
- `BK-001` 실행 전 실기기 연결/권한/로그 수집 준비 상태를 1회 점검한다.

## 체크리스트
- [ ] USB 디버깅 ON + RSA 승인 완료
- [ ] `adb devices`에서 `device` 상태 확인
- [ ] 개발자 옵션 `Stay awake` 활성화
- [ ] 배터리 최적화 제외(측정 중 백그라운드 제한 방지)
- [ ] 네트워크 상태 고정(Wi-Fi/LTE 선택 후 유지)
- [ ] 측정 대상 앱 debug 설치(`:app:installDebug`)
- [ ] logcat 태그 필터 준비(`WeeklyLottoAnalytics`, `AndroidRuntime`)
- [ ] 측정 명령 사전 리허설:
  - `./scripts/run-performance-sample-check.sh --profile device --repeat 5 --warmup 1 --serial <physical-serial>`
  - `./scripts/evaluate-performance-gate.sh --emulator-report <...> --device-report <...> --save-report <...>`
  - 통합 실행: `./scripts/run-bk-device-gate.sh --serial <physical-serial> --date-tag <yyyy-mm-dd>`
  - 대기형 실행: `./scripts/run-bk-when-physical.sh --date-tag <yyyy-mm-dd>`
  - Wear 전용 체크리스트: `docs/assets/distribution/wear_physical_device_readiness_checklist_2026-02-27.md`
  - 물리 게이트 체크포인트: `./scripts/run-physical-gates-checkpoint.sh --date-tag <yyyy-mm-dd>`
  - 물리 게이트 오케스트레이터: `./scripts/run-all-physical-gates-when-ready.sh --date-tag <yyyy-mm-dd> --save-blocked-report`

## 산출물
1. `performance_gate_device_<date>.md`
2. `performance_release_decision_<date>.md`
3. 실기기 시리얼/OS/빌드 정보 캡처
