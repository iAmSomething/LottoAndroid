# Generator Preferred Pattern Local Report (2026-03-01)

## Summary
- Scope: `IDEA-F03` v1
- Goal: Add preferred-number-pattern recommendation generation in Number Generator based on historical ticket frequency.

## Implemented
- `NumberGeneratorViewModel`
  - collect `observeAllTickets()` history snapshot.
  - add `generatePreferredPatternGames()` action.
  - add weighted recommendation generation (`pickWeightedUniqueNumbers`) with baseline weight + frequency bias.
  - reset lock state to AUTO for recommended games and emit toast summary with top-frequency numbers.
- `NumberGeneratorScreen`
  - add CTA button `선호 패턴 추천 생성`.
  - add analytics event `interaction_cta_press` (`component=preferred_pattern_generate`, `action=preferred_pattern_generate`).
- `AnalyticsActionValue`
  - add `PREFERRED_PATTERN_GENERATE` key.
- `NumberGeneratorViewModelTest`
  - add 2 regression tests for preferred pattern recommendation and empty-history fallback.

## Verification
- PASS: `./gradlew --no-daemon :app:testDebugUnitTest --tests "com.weeklylotto.app.NumberGeneratorViewModelTest"`
- FAIL (baseline): `./gradlew --no-daemon :app:detekt`
  - Existing 10 weighted issues in legacy files.

## Notes
- Detekt regression count remained at baseline (10 issues).
