#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! docker-compose ps postgres >/dev/null 2>&1; then
  echo "PostgreSQL container is not available. Start the stack first." >&2
  exit 1
fi

echo "Starting backend so Flyway migrations are applied..."
docker-compose up -d --build postgres redis backend >/dev/null

echo "Seeding local test users..."
docker-compose exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' < scripts/seed-local-users.sql

echo
echo "Local accounts:"
echo "  SUPER_ADMIN@xyz.com / SUPER_ADMIN"
echo "  ADMIN@xyz.com / ADMIN"
echo "  COMPANY_ADMIN@xyz.com / COMPANY_ADMIN"
echo
echo "These credentials are for local testing only."
