#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

PROP_FILE="${ROOT_DIR}/keystore.properties"
DRY_RUN=1
APPLY=0
FORCE=0
REPO=""

usage() {
  cat <<USAGE
Usage: $0 [--repo owner/repo] [--apply] [--dry-run] [--force]

Options:
  --repo <owner/repo>  GitHub repository nameWithOwner. If omitted, try git remote auto-detect.
  --apply              Actually upload secrets to GitHub.
  --dry-run            Validate inputs only (default mode).
  --force              Skip repository structure safety check.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)
      REPO="${2:-}"
      shift
      ;;
    --apply)
      APPLY=1
      DRY_RUN=0
      ;;
    --dry-run)
      DRY_RUN=1
      APPLY=0
      ;;
    --force)
      FORCE=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 2
      ;;
  esac
  shift
done

if [[ ! -f "$PROP_FILE" ]]; then
  echo "[ERROR] keystore.properties not found: $PROP_FILE"
  echo "        Run ./scripts/setup-local-release-signing.sh first."
  exit 1
fi

extract_prop() {
  local file="$1"
  local key="$2"
  awk -F '=' -v k="$key" '$1==k {print substr($0, index($0, "=")+1)}' "$file" | tail -n 1
}

STORE_FILE="$(extract_prop "$PROP_FILE" "storeFile")"
STORE_PASSWORD="$(extract_prop "$PROP_FILE" "storePassword")"
KEY_ALIAS="$(extract_prop "$PROP_FILE" "keyAlias")"
KEY_PASSWORD="$(extract_prop "$PROP_FILE" "keyPassword")"

if [[ -z "$STORE_FILE" || -z "$STORE_PASSWORD" || -z "$KEY_ALIAS" || -z "$KEY_PASSWORD" ]]; then
  echo "[ERROR] keystore.properties has missing values."
  exit 1
fi

if [[ ! -f "$STORE_FILE" ]]; then
  echo "[ERROR] storeFile does not exist: $STORE_FILE"
  exit 1
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "[ERROR] gh CLI not found."
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "[ERROR] gh authentication required. Run: gh auth login"
  exit 1
fi

if [[ -z "$REPO" ]]; then
  if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner 2>/dev/null || true)"
  fi
fi

detect_android_repo_from_owner() {
  local login="$1"
  local repos_output
  local repo
  local candidates

  repos_output="$(gh api -X GET "users/${login}/repos?type=owner&per_page=100" --jq '.[].full_name')"
  candidates=""
  while IFS= read -r repo; do
    [[ -z "$repo" ]] && continue
    if gh api -X GET "repos/${repo}/contents/app/build.gradle.kts" >/dev/null 2>&1; then
      if [[ -z "$candidates" ]]; then
        candidates="$repo"
      else
        candidates="${candidates}"$'\n'"${repo}"
      fi
    fi
  done <<< "$repos_output"

  local count
  count="$(printf '%s\n' "$candidates" | sed '/^$/d' | wc -l | tr -d ' ')"

  if [[ "$count" == "1" ]]; then
    printf '%s\n' "$candidates" | sed '/^$/d'
    return 0
  fi

  if [[ "$count" -gt 1 ]]; then
    echo "[ERROR] Multiple Android repo candidates found. Use --repo explicitly." >&2
    printf '%s\n' "$candidates" | sed '/^$/d' | sed 's/^/  - /' >&2
    return 2
  fi

  echo "[ERROR] No Android repo candidate found under user ${login}. Use --repo explicitly." >&2
  return 3
}

if [[ -z "$REPO" ]]; then
  LOGIN="$(gh api user -q '.login' 2>/dev/null || true)"
  if [[ -n "$LOGIN" ]]; then
    set +e
    AUTO_REPO="$(detect_android_repo_from_owner "$LOGIN")"
    AUTO_STATUS=$?
    set -e
    if [[ "$AUTO_STATUS" -eq 0 ]]; then
      REPO="$AUTO_REPO"
      echo "[INFO] Auto-detected Android repository: $REPO"
    else
      exit 1
    fi
  else
    echo "[ERROR] Could not determine repository. Use --repo owner/repo."
    exit 1
  fi
fi

looks_like_android_repo() {
  local repo="$1"
  gh api -X GET "repos/${repo}/contents/app/build.gradle.kts" >/dev/null 2>&1
}

STORE_FILE_BASE64="$(base64 < "$STORE_FILE" | tr -d '\n')"

echo "[INFO] Target repo: $REPO"
echo "[INFO] Secrets to sync:"
echo "  - LOTTO_RELEASE_STORE_FILE_BASE64"
echo "  - LOTTO_RELEASE_STORE_PASSWORD"
echo "  - LOTTO_RELEASE_KEY_ALIAS"
echo "  - LOTTO_RELEASE_KEY_PASSWORD"

if looks_like_android_repo "$REPO"; then
  echo "[PASS] Target repository looks like Android project(app/build.gradle.kts found)."
else
  if [[ "$FORCE" -eq 1 ]]; then
    echo "[WARN] Target repository does not look like Android project, but --force is set."
  else
    echo "[WARN] Target repository does not look like Android project(app/build.gradle.kts missing)."
    if [[ "$APPLY" -eq 1 ]]; then
      echo "[ERROR] Refusing to upload secrets. Re-run with --force if intentional."
      exit 1
    fi
  fi
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "[PASS] Dry-run complete. No secrets were uploaded."
  echo "[INFO] To apply: $0 --repo $REPO --apply"
  exit 0
fi

printf '%s' "$STORE_FILE_BASE64" | gh secret set LOTTO_RELEASE_STORE_FILE_BASE64 --repo "$REPO"
printf '%s' "$STORE_PASSWORD" | gh secret set LOTTO_RELEASE_STORE_PASSWORD --repo "$REPO"
printf '%s' "$KEY_ALIAS" | gh secret set LOTTO_RELEASE_KEY_ALIAS --repo "$REPO"
printf '%s' "$KEY_PASSWORD" | gh secret set LOTTO_RELEASE_KEY_PASSWORD --repo "$REPO"

echo "[PASS] Secrets uploaded successfully."
echo "[INFO] Current secret names:"
gh secret list --repo "$REPO" | rg 'LOTTO_RELEASE_' -n -S || true
