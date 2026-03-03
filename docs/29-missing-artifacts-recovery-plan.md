# Missing 산출물 해소 계획 (AN-005)

## 1. 목적
- `28-approval-package-inventory.md`에서 `missing`으로 남은 3개 산출물을 `ready`로 전환한다.
- `AB-005/AB-006` 착수 승인 전 필요한 근거를 파일 경로 기준으로 확정한다.

## 2. 대상 항목
| artifact_id | 현재 상태 | 목표 상태 |
|---|---|---|
| distribution_evidence | ready | ready |
| visual_matrix_pack | ready | ready |
| font_assets | ready | ready |

## 3. 실행 계획
| artifact_id | 실행 방법 | 목표 경로 | 담당 | 목표일 |
|---|---|---|---|---|
| distribution_evidence | Firebase 배포 dry-run 1회 실행 후 로그 보관 (완료: PR#1 merge 체인 실증) | `docs/assets/distribution/firebase_dry_run_2026-02-26.md` | dev | 2026-02-26 완료 |
| visual_matrix_pack | Home/Result/Generator/Manage 캡처(기본/저조도 × 1.0x/1.3x) 수집 (완료) | `docs/assets/visual-proof-matrix/*.png` | dev + reviewer | 2026-02-26 완료 |
| font_assets | 폰트 파일 3종 반입 + 경로/라이선스 확인 (완료) | `app/src/main/res/font/*.ttf` | dev + planner | 2026-02-26 완료 |

## 4. 완료 판정
- `distribution_evidence`: 로그 파일 1건 이상 존재 + 날짜/실행 커맨드 기록
- `visual_matrix_pack`: 필수 화면 4종 캡처 세트 존재 + `27` 12장 판정 템플릿 작성
- `font_assets`: 폰트 파일 실물 존재 + 라이선스/보관 경로 문서 링크 존재

## 5. 연동 규칙
- 상태 업데이트 기준 문서:
  - 인벤토리 상태: `28-approval-package-inventory.md`
  - 루틴 트래킹: `10-detailed-todo-board.md`, `25-routine-cycle-report.md`
- 세 항목 모두 `ready`가 되기 전에는 `AB-005/AB-006` 착수 보류
- 현재 상태(2026-02-26): `distribution_evidence`, `visual_matrix_pack`, `font_assets` 모두 `ready`
