# Figma MCP 호출 한도 점검 (2026-02-26)

## 점검 목적
- `docs/08-design-mapping.md`의 잔여 항목인 Figma node `6:2` 직접 대조 가능 여부 확인

## 실행 컨텍스트
- 대상 파일: `DY43CuXVwQwlqakFfR2yM1`
- 대상 노드: `6:2`
- 점검 순서:
  1. `mcp__figma__whoami`
  2. `mcp__figma__get_design_context(fileKey=DY43CuXVwQwlqakFfR2yM1, nodeId=6:2)`

## 결과
- 인증 계정 확인: `st939823@gmail.com` (`태훈김`)
- 디자인 컨텍스트 조회: 실패
  - 응답: `You've reached the Figma MCP tool call limit for your seat type or plan.`

## 결론
- 현재 플랜 한도 이슈로 node `6:2` 직접 메타데이터/스크린샷/코드 컨텍스트 수집 불가
- 한도 해소 전까지 `docs/19-offline-design-qa-checklist.md` 기준으로 오프라인 정합성 QA 유지
