#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

read_env_value() {
  local key="$1"
  awk -F= -v key="$key" '$1 == key {sub(/^[^=]*=/, ""); gsub(/^"|"$/, ""); print; exit}' .env 2>/dev/null || true
}

PROFILE="${SPRING_PROFILE:-$(read_env_value SPRING_PROFILE)}"
PROFILE="${PROFILE:-local}"
if [[ "$PROFILE" != "local" ]]; then
  JWT_SECRET_VALUE="${JWT_SECRET_KEY:-$(read_env_value JWT_SECRET_KEY)}"
  DB_PASSWORD_VALUE="${DB_PASSWORD:-$(read_env_value DB_PASSWORD)}"
  if [[ -z "$JWT_SECRET_VALUE" || "$JWT_SECRET_VALUE" == *"change"* || ${#JWT_SECRET_VALUE} -lt 32 ]]; then
    echo "JWT_SECRET_KEY must be a production-strength secret (minimum 32 characters)." >&2
    exit 1
  fi
  if [[ -z "$DB_PASSWORD_VALUE" || "$DB_PASSWORD_VALUE" == "postgres" || "$DB_PASSWORD_VALUE" == "password" ]]; then
    echo "DB_PASSWORD must not use a development default outside the local profile." >&2
    exit 1
  fi
fi

docker-compose config >/dev/null

echo "Preflight checks passed for profile: $PROFILE"
