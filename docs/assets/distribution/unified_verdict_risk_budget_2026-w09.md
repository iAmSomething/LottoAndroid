# Unified Verdict Risk Budget (2026-W09)

- Week: 2026-W09
- Conditional count: 0
- Hold count: 2
- Risk budget used: 2.0
- Repeated escalation codes: E13-1:2, E13-2:0, E13-3:0, E13-4:0
- Freeze status: global
- Exception approval: none
- Recovery ETA: 2026-03-01 18:00 KST (실기기 확보 및 BK-001~BK-002 완료 가정)

## S15 검증 결과
1. `S15-1` 위험예산 계산
   - 산식: `(조건부 * 0.5) + (보류 * 1.0)`
   - 결과: `(0 * 0.5) + (2 * 1.0) = 2.0`
   - 판정: `FAIL` 경계값 도달(기준 `>= 2.0`)

2. `S15-2` 프리즈 트리거 반영
   - `보류` 1건 이상 + 소진율 FAIL 조건 충족으로 `global freeze` 유지
   - 반영 상태: 릴리즈 진행 차단, BK 트랙 우선 처리

3. `S15-3` 해제 조건 검증
   - 조건: 2개 빌드 연속 재발 0건 + `S10/S11/S12/S13` PASS + 액션게이트 95%+
   - 현재: `E13-1` 재발 2건, BK 액션 완료율 0%
   - 판정: 해제 조건 미충족(검증 완료, 상태 유지)

4. `S15-4` 예외 승인 검증
   - 보류 건에 대한 예외 승인: 정책상 불가(검증 완료)
   - 조건부 진행 예외 샘플 기록:
     - approvers: 제품 오너/릴리즈 오너
     - reason(3 lines): 운영 캘린더 충돌, 영향 범위 제한, 복구 계획 존재
     - scope: 통계 화면 비차단 이슈 1건
     - ETA: 2026-03-01 12:00 KST
   - 판정: 필수 필드 누락 없음

## Next controls
1. BK-001 실기기 device 성능 리포트 생성.
2. BK-002 성능 판정 재평가 후 freeze 상태 재판정.
3. BQ-002 템플릿 기반 핫패스 실측 보강.

## Linked evidence
- `docs/assets/distribution/unified_verdict_weekly_2026-w09.md`
- `docs/assets/distribution/unified_verdict_dryrun_2026-02-26.md`
- `docs/assets/distribution/unified_verdict_2026-02-26.md`
