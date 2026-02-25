#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

KEY_HOME="${HOME}/.weeklylotto/keys"
STORE_FILE="${KEY_HOME}/weeklylotto-release.jks"
KEY_ALIAS="weeklylotto"
CREDENTIAL_FILE="${KEY_HOME}/weeklylotto-release-credentials.txt"
BACKUP_DIR="${KEY_HOME}/backup"
PROP_FILE="${ROOT_DIR}/keystore.properties"

mkdir -p "$KEY_HOME" "$BACKUP_DIR"

random_secret() {
  openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | cut -c1-24
}

load_existing_property() {
  local file="$1"
  local key="$2"
  if [[ ! -f "$file" ]]; then
    return 0
  fi
  awk -F '=' -v k="$key" '$1==k {print substr($0, index($0, "=")+1)}' "$file" | tail -n 1
}

# If keystore.properties already has complete values, normalize and validate.
EXISTING_STORE_FILE="$(load_existing_property "$PROP_FILE" "storeFile")"
EXISTING_STORE_PASSWORD="$(load_existing_property "$PROP_FILE" "storePassword")"
EXISTING_KEY_ALIAS="$(load_existing_property "$PROP_FILE" "keyAlias")"
EXISTING_KEY_PASSWORD="$(load_existing_property "$PROP_FILE" "keyPassword")"

if [[ -n "${EXISTING_STORE_FILE:-}" && -n "${EXISTING_STORE_PASSWORD:-}" && -n "${EXISTING_KEY_ALIAS:-}" && -n "${EXISTING_KEY_PASSWORD:-}" ]]; then
  STORE_FILE="$EXISTING_STORE_FILE"
  STORE_PASSWORD="$EXISTING_STORE_PASSWORD"
  KEY_ALIAS="$EXISTING_KEY_ALIAS"
  # PKCS12 keystore is commonly used here; keep key password aligned with store password.
  KEY_PASSWORD="$STORE_PASSWORD"

  if [[ ! -f "$STORE_FILE" ]]; then
    echo "[ERROR] Configured storeFile does not exist: $STORE_FILE"
    exit 1
  fi

  if ! keytool -list -keystore "$STORE_FILE" -storepass "$STORE_PASSWORD" -alias "$KEY_ALIAS" >/dev/null 2>&1; then
    echo "[ERROR] Existing keystore config failed verification. Please check keystore.properties."
    exit 1
  fi

  {
    echo "storeFile=$STORE_FILE"
    echo "storePassword=$STORE_PASSWORD"
    echo "keyAlias=$KEY_ALIAS"
    echo "keyPassword=$KEY_PASSWORD"
  } > "$PROP_FILE"
  chmod 600 "$PROP_FILE"

  {
    echo "storeFile=$STORE_FILE"
    echo "storePassword=$STORE_PASSWORD"
    echo "keyAlias=$KEY_ALIAS"
    echo "keyPassword=$KEY_PASSWORD"
  } > "$CREDENTIAL_FILE"
  chmod 600 "$CREDENTIAL_FILE"

  echo "[INFO] Existing keystore configuration normalized."
  echo "[PASS] Release signing configured successfully."
  exit 0
fi

if [[ -f "$STORE_FILE" ]]; then
  echo "[INFO] Existing keystore found: $STORE_FILE"
  if [[ -f "$CREDENTIAL_FILE" ]]; then
    STORE_PASSWORD="$(load_existing_property "$CREDENTIAL_FILE" "storePassword")"
    KEY_PASSWORD="$(load_existing_property "$CREDENTIAL_FILE" "keyPassword")"
  else
    echo "[ERROR] Existing keystore found but credential file missing: $CREDENTIAL_FILE"
    echo "        Please set keystore.properties manually."
    exit 1
  fi
else
  STORE_PASSWORD="$(random_secret)"
  KEY_PASSWORD="$STORE_PASSWORD"

  keytool -genkeypair \
    -v \
    -keystore "$STORE_FILE" \
    -alias "$KEY_ALIAS" \
    -storepass "$STORE_PASSWORD" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=WeeklyLotto, OU=Mobile, O=WeeklyLotto, L=Seoul, ST=Seoul, C=KR" >/dev/null

  chmod 600 "$STORE_FILE"

  {
    echo "storeFile=$STORE_FILE"
    echo "storePassword=$STORE_PASSWORD"
    echo "keyAlias=$KEY_ALIAS"
    echo "keyPassword=$KEY_PASSWORD"
  } > "$CREDENTIAL_FILE"
  chmod 600 "$CREDENTIAL_FILE"

  TS="$(date +%Y%m%d-%H%M%S)"
  cp "$STORE_FILE" "$BACKUP_DIR/weeklylotto-release-$TS.jks"
  shasum -a 256 "$STORE_FILE" > "$BACKUP_DIR/weeklylotto-release-$TS.sha256"

  echo "[INFO] New release keystore created: $STORE_FILE"
  echo "[INFO] Backup created: $BACKUP_DIR/weeklylotto-release-$TS.jks"
fi

if [[ -z "${STORE_PASSWORD:-}" || -z "${KEY_PASSWORD:-}" ]]; then
  echo "[ERROR] Store/key password is empty."
  exit 1
fi

{
  echo "storeFile=$STORE_FILE"
  echo "storePassword=$STORE_PASSWORD"
  echo "keyAlias=$KEY_ALIAS"
  echo "keyPassword=$KEY_PASSWORD"
} > "$PROP_FILE"
chmod 600 "$PROP_FILE"

if keytool -list -keystore "$STORE_FILE" -storepass "$STORE_PASSWORD" -alias "$KEY_ALIAS" >/dev/null 2>&1; then
  echo "[PASS] Release signing configured successfully."
  echo "[PASS] keystore.properties generated at: $PROP_FILE"
  echo "[PASS] Credential mirror stored at: $CREDENTIAL_FILE"
else
  echo "[ERROR] keytool verification failed."
  exit 1
fi
