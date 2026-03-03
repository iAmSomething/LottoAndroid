# 승인 패키지 인벤토리 (AJ-005 / AM-005)

## 1. 목적
- `AJ-005` 승인 패키지 산출물을 실제 파일 경로 기준으로 관리한다.
- 착수 승인 전 `ready/missing/revise` 상태를 한 번에 판단할 수 있게 한다.

## 2. 기준 스냅샷
- 기준일: 2026-02-26 (Cycle-26)
- 코드/품질 기준선:
  - `app/main=120`, `app/test=21`, `app/androidTest=5`, `wear/main=9`
  - `./gradlew :app:assembleDebug :app:testDebugUnitTest :wear:assembleDebug` 성공

## 3. 인벤토리
| artifact_id | path | status | owner | notes |
|---|---|---|---|---|
| checklist_policy | `docs/27-ui-visual-polish-pack.md` | ready | planner | 10장(`AJ-005`), 12장(`AL-005`), 13장(`AM-005`) 기준 존재 |
| checklist_board | `docs/10-detailed-todo-board.md` | ready | planner | `AJ-005`, `AK-005`, `AL-005`, `AM-005` 추적 라인 존재 |
| distribution_script | `scripts/firebase-distribute.sh` | ready | dev | 배포 실행 경로 존재 |
| distribution_evidence | `docs/assets/distribution/firebase_dry_run_2026-02-26.md` | ready | dev | 2026-02-26 PR#1 merge 체인 증적(`Release Preflight` 22432219038, `Firebase Distribution` 22432295422) |
| visual_baseline_pack | `docs/assets/typography-refresh/*.png` | ready | dev | `home/result before/after`, `font_1_3x`, `splash` 캡처 존재 |
| visual_matrix_pack | `docs/assets/visual-proof-matrix/*.png` | ready | dev | 4화면 x normal/lowlight x 1.0/1.3 캡처 세트 생성(`32-visual-proof-matrix-report.md`) |
| font_assets | `app/src/main/res/font/*.ttf` | ready | dev | 브랜드 폰트 3종 반입 + `Type.kt` 매핑 완료(`31-font-assets-and-license-register.md`) |
| qa_checklist | `docs/19-offline-design-qa-checklist.md` | ready | reviewer | 오프라인 시각 QA 체크리스트 존재 |

## 4. 승인 판정 규칙
- 착수 승인 최소 조건:
  - `distribution_evidence`, `visual_matrix_pack`, `font_assets`를 `ready`로 전환
  - `AF-005`, `AB-005`, `AB-006`과 경로 기준으로 상호 참조 확인
- 보류 조건:
  - 위 3개 항목 중 하나라도 `missing`이면 `AB-005/AB-006` 착수 보류

## 5. 해소 계획 연동
- 해소 계획 문서: `29-missing-artifacts-recovery-plan.md`
- 운영 방식:
  - `29`에서 담당/기한/목표 경로를 관리
  - 각 항목 완료 시 본 문서의 `status`를 `ready`로 갱신
