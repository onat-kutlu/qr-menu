#!/usr/bin/env bash
set -euo pipefail

DATE_SUFFIX=$(date +"%Y-%m-%d_%H-%M-%S")
BACKUP_DIR="${BACKUP_DIR:-/storage/backups/postgres}"
DB_NAME="${DB_NAME:-qrmenu}"
DB_USER="${DB_USER:-postgres}"
DB_HOST="${DB_HOST:-localhost}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"

mkdir -p "$BACKUP_DIR"

FILE_PATH="$BACKUP_DIR/${DB_NAME}-${DATE_SUFFIX}.sql.gz"

PGPASSWORD="${DB_PASSWORD:-postgres}" pg_dump -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" --clean --if-exists | gzip > "$FILE_PATH"

find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

echo "Database backup created: $FILE_PATH"
