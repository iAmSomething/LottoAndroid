# S39-T2 번호 직접 추가 중복 선택지 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.ManualAddViewModelTest"
```

## 결과
- `ManualAddViewModelTest` PASS
  - 중복 감지 시 선택지 상태(`duplicatePrompt`) 노출 확인
  - `중복 제외 저장` 선택 시 신규 게임만 저장 확인
  - `중복 포함 저장` 선택 시 중복 게임 포함 저장 확인

## 반영 범위
- `ManualAddViewModel` 중복 저장 의사결정 플로우 추가
- `ManualAddScreen` 중복 경고 카드 + `취소/중복 제외 저장/중복 포함 저장` 버튼 추가
