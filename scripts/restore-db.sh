#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <backup-file.sql.gz>"
  exit 1
fi

BACKUP_FILE="$1"
DB_NAME="${DB_NAME:-qrmenu}"
DB_USER="${DB_USER:-postgres}"
DB_HOST="${DB_HOST:-localhost}"

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Backup file not found: $BACKUP_FILE"
  exit 1
fi

PGPASSWORD="${DB_PASSWORD:-postgres}" gunzip -c "$BACKUP_FILE" | psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME"

echo "Database restored from: $BACKUP_FILE"
