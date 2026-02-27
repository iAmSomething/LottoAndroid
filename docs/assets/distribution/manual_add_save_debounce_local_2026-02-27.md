# S39-T5 번호 직접 추가 저장 debounce 점검 리포트 (2026-02-27)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.ManualAddViewModelTest"
```

## 결과
- `ManualAddViewModelTest` PASS
  - 저장 요청 연속 탭 시 저장 요청 1회 처리 확인
  - `isSaving` 상태가 저장 완료 후 false로 복귀하는지 확인
  - 기존 중복/복원/undo 시나리오 회귀 통과 확인

## 반영 범위
- `ManualAddViewModel` 저장 in-flight 가드(`isSaving`) 추가
- `ManualAddScreen` 저장/중복 선택 버튼 비활성화 및 `저장 중...` 안내 추가
