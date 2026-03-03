# Wear Physical Device Readiness Checklist (2026-02-27)

## 목적
- `P-004` 실행 전 Wear 실기기 2종(소형/대형) 연결/권한/증적 수집 준비 상태를 점검한다.

## 체크리스트
- [ ] Wear 소형/대형 실기기 각 1대 USB/무선 디버깅 연결
- [ ] `adb devices`에서 두 디바이스 모두 `device` 상태 확인
- [ ] 두 디바이스 모두 `ro.build.characteristics`에 `watch` 포함 확인
- [ ] 워치 화면 Always-on 비활성화 및 절전 모드 해제
- [ ] 배터리 40% 이상 확보(측정 중 전원 정책 변동 방지)
- [ ] `:wear:installDebug` 사전 설치 확인
- [ ] logcat 수집 준비(`AndroidRuntime`, `WearableApp`, `WeeklyLotto`)
- [ ] 증적 명령 사전 리허설:
  - `./scripts/run-p4-wear-proof-gate.sh --small-serial <small> --large-serial <large> --date-tag <yyyy-mm-dd>`
  - 대기형 실행: `./scripts/run-p4-when-wear-physical.sh --date-tag <yyyy-mm-dd>`
  - 통합 오케스트레이터 실행: `./scripts/run-all-physical-gates-when-ready.sh --date-tag <yyyy-mm-dd> --save-blocked-report`

## 산출물
1. `wear_p4_device_evidence_<date>.md`
2. `wear_p4_<date>/screenshots/*.png`
3. `wear_p4_<date>/logs/*.log`
