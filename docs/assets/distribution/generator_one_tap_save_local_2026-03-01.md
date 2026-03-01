# A01 v1 번호 생성 원탭 저장 점검 리포트 (2026-03-01)

## 실행 커맨드
```bash
./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.NumberGeneratorViewModelTest"
```

## 결과
- `NumberGeneratorViewModelTest` PASS
  - `regenerateAndSaveAsWeeklyTicket` 호출 시 재생성 1회 수행 확인
  - 재생성 결과 번호가 저장 번들에 반영되는지 확인
  - 원탭 저장 성공 메시지 노출 확인

## 반영 범위
- `NumberGeneratorViewModel`에 `regenerateAndSaveAsWeeklyTicket` 추가
- Generator 화면에 `랜덤 생성 후 바로 저장` CTA 추가
- 기존 `잠금 제외 랜덤 재생성`/`이번 주 번호로 저장하기` 동선은 유지
