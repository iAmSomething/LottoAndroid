# 폰트 온보딩 착수 게이트 (AH-005 / AI-005)

## 1. 목적
- `AB-005`(실제 브랜드 폰트 도입) 착수 전 필수 점검 항목을 체크리스트로 고정한다.
- 배포 검증 증적과 결합해 코드 착수 승인 기준(`AI-005`)을 명시한다.

## 2. 범위
- 대상 태스크: `AG-005`, `AH-005`, `AI-005`, `AB-005`, `AB-006`
- 대상 경로:
  - 폰트 리소스: `app/src/main/res/font/*.ttf`
  - 배포 증적: `docs/assets/distribution/firebase_dry_run_2026-02-26.md`
  - 인벤토리: `docs/28-approval-package-inventory.md`

## 3. 통합 체크리스트 (AH-005)
- 파일 준비
  - [x] 브랜드 폰트 파일 3종 이상 확보(예: headline/body/numeric)
  - [x] 파일명 규칙 확정(`brand_roboto_condensed_variable.ttf`, `brand_noto_sans_kr_variable.ttf`, `brand_roboto_mono_variable.ttf`)
  - [x] `app/src/main/res/font/` 반입 가능 여부 확인
- 라이선스/보관
  - [x] 상업적 사용 가능 라이선스 조항 확인
  - [x] 라이선스 원문 보관 경로 확정(사내 저장소 또는 계약 문서 링크)
  - [x] 재배포/수정 가능 범위 체크
- 적용 순서
  - [x] `Type.kt`에 신규 `FontFamily` 매핑 순서 정의
  - [x] fallback(`SansSerif/Monospace`) 유지 정책 정의
  - [x] 적용 우선 화면(Home → Result → Generator → Manage) 고정
- 검증
  - [x] `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest :app:assembleDebug` 통과
  - [x] 1.3x 폰트 스케일 가독성 점검 캡처 확보

## 4. 코드 착수 승인 기준 (AI-005)
- `AB-005` 코드 착수는 아래 조건을 모두 만족할 때만 승인한다.
  1. `distribution_evidence`가 `ready` 상태일 것
     - 기준 증적: `docs/assets/distribution/firebase_dry_run_2026-02-26.md`
  2. 3장 통합 체크리스트의 문서 항목이 확정될 것
     - 최소 충족: 파일명 규칙, 라이선스 검증 절차, 적용 순서 정의
  3. 인벤토리 상태가 최신일 것
     - `docs/28-approval-package-inventory.md`의 `distribution_evidence`가 `ready`
     - `font_assets`는 실제 착수 전까지 `missing` 유지 가능하나, 반입 계획은 확정

## 5. 현재 판정 (2026-02-26)
- `distribution_evidence`: ready
- `font_assets`: ready
- 승인 결과: `코드 착수/반영 완료` (`AB-005`, `AB-006` 기준 충족)

## 6. 다음 실행
1. 분기 1회 라이선스 출처 URL 유효성 점검(`31`의 링크와 라이선스 원문 경로 재검증)
2. 신규 화면 추가 시 `32` 매트릭스 규칙으로 1.3x/저조도 캡처 1세트 추가
3. 폰트 파일 갱신 시 `Type.kt` 가중치 매핑 회귀 테스트 + 품질게이트 재실행
