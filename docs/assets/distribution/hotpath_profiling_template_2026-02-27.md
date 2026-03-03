# Hotpath Profiling Template (S06) (2026-02-27)

## 대상 구간
1. Home 초기 진입
2. Result 회차 변경 시트 열기/적용
3. Manage 필터 시트 열기/적용

## 측정 항목
- startup(ms): cold/warm
- render(ms): first meaningful render
- jank(%): interaction 구간 프레임 저하율
- notes: 재현 조건/기기 상태/네트워크

## 기록 표
| Screen | Scenario | startup | render | jank | verdict | notes |
|---|---|---:|---:|---:|---|---|
| Home | cold start | - | - | - | pending | |
| Result | round sheet apply | - | - | - | pending | |
| Manage | filter apply | - | - | - | pending | |

## 판정 기준
- startup P95: 1200ms 이하
- jank: 3% 이하
- render: baseline 대비 악화 없음(또는 절대 임계치 충족)

## 산출물 경로
- `docs/assets/distribution/hotpath_s06_profile_<date>.md`
- 연결 체크리스트: `docs/07-release-checklist.md` `S06`
