#!/bin/bash
# PostgreSQL Restore Script for Nadi
# Usage: ./restore.sh <backup_file> [container_name] [db_name] [db_user]

set -euo pipefail

if [ $# -lt 1 ]; then
    echo "Usage: $0 <backup_file.sql.gz> [container_name] [db_name] [db_user]"
    exit 1
fi

BACKUP_FILE="$1"
CONTAINER="${2:-nadi-db}"
DB_NAME="${3:-nadi}"
DB_USER="${4:-nadi}"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "ERROR: Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "[$(date)] WARNING: This will OVERWRITE the database '${DB_NAME}'!"
echo "[$(date)] Backup file: ${BACKUP_FILE}"
read -p "Continue? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Aborted."
    exit 0
fi

echo "[$(date)] Dropping and recreating database..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d postgres -c "DROP DATABASE IF EXISTS ${DB_NAME};"
docker exec "$CONTAINER" psql -U "$DB_USER" -d postgres -c "CREATE DATABASE ${DB_NAME};"

echo "[$(date)] Restoring from ${BACKUP_FILE}..."
gunzip -c "$BACKUP_FILE" | docker exec -i "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" --quiet

echo "[$(date)] Restore complete."
echo "[$(date)] Verifying..."
docker exec "$CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';"
echo "[$(date)] Done."
