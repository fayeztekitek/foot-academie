#!/bin/bash
# PostgreSQL Backup Script for Nadi
# Usage: ./backup.sh [container_name] [db_name] [db_user]
# Schedule: crontab -e → 0 2 * * * /path/to/backup.sh

set -euo pipefail

CONTAINER="${1:-nadi-db}"
DB_NAME="${2:-nadi}"
DB_USER="${3:-nadi}"
BACKUP_DIR="/backups/nadi"
RETENTION_DAYS=30
DATE=$(date +%Y%m%d_%H%M%S)
FILENAME="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Starting backup of ${DB_NAME} from ${CONTAINER}..."

docker exec "$CONTAINER" pg_dump -U "$DB_USER" -d "$DB_NAME" --no-owner --no-acl \
    | gzip > "$FILENAME"

FILESIZE=$(du -h "$FILENAME" | cut -f1)
echo "[$(date)] Backup completed: ${FILENAME} (${FILESIZE})"

# Cleanup old backups
echo "[$(date)] Cleaning backups older than ${RETENTION_DAYS} days..."
find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +"$RETENTION_DAYS" -delete

REMAINING=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" | wc -l)
echo "[$(date)] ${REMAINING} backup(s) remaining."
echo "[$(date)] Backup job finished."
