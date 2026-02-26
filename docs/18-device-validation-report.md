# 실기기 계측 검증 리포트

## 목적
- `connectedDebugAndroidTest`를 실제 Android 디바이스 1대 이상에서 재검증한 이력을 기록한다.

## 실행 방법
```bash
./scripts/run-physical-device-validation.sh
```

옵션:
- 특정 기기 지정: `./scripts/run-physical-device-validation.sh --serial <adb-serial>`
- 문서 업데이트 생략: `./scripts/run-physical-device-validation.sh --skip-doc`

## 실행 이력
- 2026-02-26: 실기기 미연결로 실행 불가 (`[FAIL] No physical device connected. Connect one device and retry.`)
- 2026-02-26: 프리플라이트 엄격 모드에서도 동일 확인 (`./scripts/release-preflight.sh --with-build --require-physical-device` → `FAIL=1`)
