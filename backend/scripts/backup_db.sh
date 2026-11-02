#!/bin/bash
# Back up the database into a compressed file, then delete the old backups.
# Run every day on the server by cron.

# Stop at the first error, even inside a pipe
set -euo pipefail

# Default values for the server; each one can be changed when running the script
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.prod.yml}"
BACKUP_DIR="${BACKUP_DIR:-/root/backups}"
KEEP_DAYS="${KEEP_DAYS:-14}"

# Go to the backend folder, where the docker-compose files are
cd "$(dirname "$0")/.."

mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/melodrop-$(date +%F-%H%M).sql.gz"

# Export the whole database and compress it, in a temporary file first
docker compose -f "$COMPOSE_FILE" exec -T db sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction "$MYSQL_DATABASE"' | gzip > "$FILE.tmp"

# Only a complete export gets the final name
mv "$FILE.tmp" "$FILE"

# Delete the backups older than KEEP_DAYS days
find "$BACKUP_DIR" -name "melodrop-*" -mtime +"$KEEP_DAYS" -delete

echo "$(date '+%F %T') backup saved: $FILE"