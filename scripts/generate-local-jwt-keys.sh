#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SECRETS_DIR="${APP_LOCAL_SECRETS_DIR:-${ROOT_DIR}/platform/secrets/local}"
PRIVATE_KEY_FILE="${SECRETS_DIR}/identity-access-private.pem"
PUBLIC_KEY_FILE="${SECRETS_DIR}/identity-access-public.pem"

mkdir -p "${SECRETS_DIR}"

if ! command -v openssl >/dev/null 2>&1; then
  echo "Missing required command: openssl"
  exit 1
fi

if [[ -f "${PRIVATE_KEY_FILE}" && -f "${PUBLIC_KEY_FILE}" ]]; then
  echo "Local JWT keys already exist in ${SECRETS_DIR}"
  exit 0
fi

openssl genrsa -out "${PRIVATE_KEY_FILE}" 2048 >/dev/null 2>&1
openssl rsa -in "${PRIVATE_KEY_FILE}" -pubout -out "${PUBLIC_KEY_FILE}" >/dev/null 2>&1

chmod 600 "${PRIVATE_KEY_FILE}"
chmod 644 "${PUBLIC_KEY_FILE}"

echo "Generated local JWT keypair:"
echo "  private: ${PRIVATE_KEY_FILE}"
echo "  public : ${PUBLIC_KEY_FILE}"
