# S01/A05 하드닝 게이트 검증 (2026-02-26)

## 1) 실행 명령
- `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest --tests "com.weeklylotto.app.AppErrorCategoryTest" --tests "com.weeklylotto.app.ResultErrorUiTest" :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest --tests "com.weeklylotto.app.data.network.DrawApiClientTest" --tests "com.weeklylotto.app.ResultViewModelTest"`

## 2) 결과
- 두 명령 모두 `BUILD SUCCESSFUL`
- `S01` 검증 포인트
  - 오류 카테고리 분류 테스트: `AppErrorCategoryTest` (timeout/http_4xx/http_5xx/schema/storage_full)
  - 사용자 메시지 매핑 테스트: `ResultErrorUiTest`
  - 운영 로그 error_type 분류: `DrawApiClient`가 `toErrorCategory()` 기준으로 `ops_api_request` 기록
- `A05` 검증 포인트
  - fallback 공통 컴포넌트 도입: `ExternalOpenFallbackDialog`
  - Home 구매 리다이렉트 실패 시 `external_open_failed` + `url` 필드 로깅
  - fallback 액션(`링크 복사`, `기본 브라우저로 열기`) 동작 경로를 단일 컴포넌트로 통합

## 3) 근거 파일
- `app/src/main/java/com/weeklylotto/app/domain/error/AppErrorCategory.kt`
- `app/src/main/java/com/weeklylotto/app/feature/result/ResultErrorUi.kt`
- `app/src/main/java/com/weeklylotto/app/ui/component/ExternalOpenFallbackDialog.kt`
- `app/src/main/java/com/weeklylotto/app/feature/home/HomeScreen.kt`
- `app/src/test/java/com/weeklylotto/app/AppErrorCategoryTest.kt`
- `app/src/test/java/com/weeklylotto/app/ResultErrorUiTest.kt`
