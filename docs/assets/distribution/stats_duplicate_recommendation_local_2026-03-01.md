# Stats Duplicate Recommendation Local Report (2026-03-01)

## Summary
- Scope: `C01` v2
- Goal: Extend duplicate-combination insight with actionable recommendation and Generator CTA from Stats.

## Implemented
- `StatsViewModel`: `DuplicateCombinationInsight` model + duplicate rate/count/most-repeated combination/level/recommendation calculation.
- `StatsScreen`: added `조합 중복도 경고` card and `중복 줄이기 번호 생성` CTA.
- Navigation: `StatsScreen` CTA now navigates to Generator.
- Analytics: `interaction_cta_press` with `component=duplicate_warning_card`, `action=duplicate_warning_generate`.
- Tests: `StatsViewModelTest` added 2 cases (duplicate warning and stable state).

## Verification
- PASS: `./gradlew --no-daemon :app:testDebugUnitTest --tests "com.weeklylotto.app.StatsViewModelTest"`
- FAIL (pre-existing baseline): `./gradlew --no-daemon :app:detekt`
  - Existing 10 weighted issues in unrelated and legacy files.
- FAIL (pre-existing baseline): `./gradlew --no-daemon :app:ktlintCheck`
  - Existing formatting/import-order violations in legacy test files.

## Notes
- This cycle does not add new detekt regression beyond repository baseline.
