#!/usr/bin/env bash
set -euo pipefail

FREEZE_TYPE="${1:-global}"
REASON="${2:-risk_budget_fail}"
SCOPE="${3:-release_pipeline}"
NEXT_UPDATE="${4:-+120min}"

cat <<EOF
[Freeze Notice]
- type: ${FREEZE_TYPE}
- reason: ${REASON}
- scope: ${SCOPE}
- status: active
- next_update: ${NEXT_UPDATE}

[Required fields]
- escalation_code: <E13-*>
- owner: <release-owner>
- eta: <YYYY-MM-DD HH:mm>
EOF
