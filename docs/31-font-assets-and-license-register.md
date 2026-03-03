# 폰트 자산/라이선스 레지스터 (AC-005 / AG-005 / AB-005)

## 1. 목적
- 브랜드 폰트 1차 도입(`AB-005`)을 위한 파일/출처/라이선스/적용 경로를 고정한다.
- `AC-005`, `AG-005` 착수 전 체크리스트를 파일 경로 기준으로 완료 상태로 관리한다.

## 2. 반입 자산
| role | file | target path | notes |
|---|---|---|---|
| Display | `brand_roboto_condensed_variable.ttf` | `app/src/main/res/font/brand_roboto_condensed_variable.ttf` | headline/title 중심 |
| Body | `brand_noto_sans_kr_variable.ttf` | `app/src/main/res/font/brand_noto_sans_kr_variable.ttf` | 본문/라벨 중심 |
| Numeric | `brand_roboto_mono_variable.ttf` | `app/src/main/res/font/brand_roboto_mono_variable.ttf` | 회차/금액/번호 숫자 토큰 |

## 3. 출처
- Noto Sans KR
  - 파일: [Google Fonts/ofl/notosanskr](https://github.com/google/fonts/tree/main/ofl/notosanskr)
  - 다운로드 사용 파일: `NotoSansKR[wght].ttf`
- Roboto Condensed
  - 파일: [Google Fonts/ofl/robotocondensed](https://github.com/google/fonts/tree/main/ofl/robotocondensed)
  - 다운로드 사용 파일: `RobotoCondensed[wght].ttf`
- Roboto Mono
  - 파일: [Google Fonts/ofl/robotomono](https://github.com/google/fonts/tree/main/ofl/robotomono)
  - 다운로드 사용 파일: `RobotoMono[wght].ttf`

## 4. 라이선스 보관 경로
- `docs/assets/fonts/licenses/OFL-NotoSansKR.txt`
- `docs/assets/fonts/licenses/OFL-RobotoCondensed.txt`
- `docs/assets/fonts/licenses/OFL-RobotoMono.txt`

## 5. 적용 매핑
- 코드 경로: `app/src/main/java/com/weeklylotto/app/ui/theme/Type.kt`
- 매핑 정책:
  - `LottoDisplayFontFamily` -> Roboto Condensed variable
  - `LottoBodyFontFamily` -> Noto Sans KR variable
  - `LottoNumericFontFamily` -> Roboto Mono variable

## 6. 체크리스트 판정
- [x] 폰트 파일 3종 반입 완료(`res/font`)
- [x] 라이선스 원문 파일 3종 보관 완료
- [x] 출처 URL 문서화 완료
- [x] 적용 경로(`Type.kt`) 반영 완료
