# S07-1/S07-2 하드닝 게이트 검증 (2026-02-26)

## 1) 실행 명령
- `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest --tests "com.weeklylotto.app.AppErrorCategoryTest" --tests "com.weeklylotto.app.ResultErrorUiTest" --tests "com.weeklylotto.app.data.network.DrawApiClientTest" --tests "com.weeklylotto.app.RoomTicketRepositoryIntegrationTest" :app:assembleDebug`

## 2) 결과
- `BUILD SUCCESSFUL`
- `S07-1` 확인
  - 네트워크 오류 분류: `timeout/http_4xx/http_5xx/schema/unknown`
  - 로그 일관성: `DrawApiClient`의 `ops_api_request.error_type`가 `AppErrorCategory.analyticsValue` 기준으로 기록
  - UI 일관성: `ResultErrorUi`가 분류 기준 메시지를 노출
- `S07-2` 확인
  - 저장소 오류 분류: `storage_full/disk_io/migration/storage`
  - 저장소 로그 일관성: `RoomTicketRepository`의 `ops_storage_mutation.error_type`가 분류 기준으로 기록
  - 사용자 메시지 분기: `ResultErrorUi`에 `storage_full/disk_io/migration` 안내 추가

## 3) 근거 파일
- `app/src/main/java/com/weeklylotto/app/domain/error/AppErrorCategory.kt`
- `app/src/main/java/com/weeklylotto/app/data/network/DrawApiClient.kt`
- `app/src/main/java/com/weeklylotto/app/data/repository/RoomTicketRepository.kt`
- `app/src/main/java/com/weeklylotto/app/feature/result/ResultErrorUi.kt`
- `app/src/test/java/com/weeklylotto/app/AppErrorCategoryTest.kt`
- `app/src/test/java/com/weeklylotto/app/ResultErrorUiTest.kt`
