# 시각 증적 매트릭스 리포트 (AB-009 / AB-010 / AK-005 / AJ-005)

## 1. 목적
- `AB-009`: Home/Result, Generator/Manage 전/후 2세트 증적 확보
- `AB-010`/`AK-005`: 화면 4종 x 폰트스케일(1.0/1.3) x 저조도 조건 증적 확보
- `AJ-005`: 타이포 시안 2종(Home/Result) 증적 패키지 확정

## 2. 캡처 기준
- 기기: Android Emulator (`emulator-5554`)
- 앱: `com.weeklylotto.app.debug`
- 자동화 스크립트: `scripts/capture-visual-matrix.sh`
- 조건:
  - 밝기/모드: `normal`, `lowlight(cmd uimode night yes)`
  - 폰트 스케일: `1.0`, `1.3`
  - 파일 패턴: `{screen}_{brightness}_{fontScale}_b.png`

## 3. AB-009 전/후 2세트
- 경로: `docs/assets/typography-approval/`
- 포함 파일:
  - `home_before.png`, `home_after.png`
  - `result_before.png`, `result_after.png`
  - `generator_before.png`, `generator_after.png`
  - `manage_before.png`, `manage_after.png`

## 4. AJ-005 타이포 시안 2종(Home/Result)
- 경로: `docs/assets/typography-variants/`
- 시안 A(기존 리프레시 기준)
  - `home_variant_a.png`
  - `result_variant_a.png`
- 시안 B(브랜드 폰트 적용 기준)
  - `home_variant_b.png`
  - `result_variant_b.png`

## 5. AK-005 시각 증적 매트릭스
- 경로: `docs/assets/visual-proof-matrix/`
- Home
  - `home_normal_1_0_b.png`
  - `home_normal_1_3_b.png`
  - `home_lowlight_1_0_b.png`
  - `home_lowlight_1_3_b.png`
- Generator
  - `generator_normal_1_0_b.png`
  - `generator_normal_1_3_b.png`
  - `generator_lowlight_1_0_b.png`
  - `generator_lowlight_1_3_b.png`
- Manage
  - `manage_normal_1_0_b.png`
  - `manage_normal_1_3_b.png`
  - `manage_lowlight_1_0_b.png`
  - `manage_lowlight_1_3_b.png`
- Result
  - `result_normal_1_0_b.png`
  - `result_normal_1_3_b.png`
  - `result_lowlight_1_0_b.png`
  - `result_lowlight_1_3_b.png`

## 6. QA 판정
- [x] Home/Result 카드 레이어(배경/깊이) 반영 확인
- [x] Generator/Manage 아이콘 스타일 규격화 반영 확인
- [x] 폰트 스케일 1.3에서 핵심 정보 가독성 유지 확인
- [x] 저조도 모드에서 텍스트/CTA 대비 유지 확인
