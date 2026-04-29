#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
SEED_FILE="$ROOT_DIR/src/main/resources/db/demo/V100__seed_demo_data.sql"

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing .env file."
  echo "Create it first, for example from .env.example."
  exit 1
fi

if [ ! -f "$SEED_FILE" ]; then
  echo "Missing seed SQL file:"
  echo "$SEED_FILE"
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

DB_SERVICE="${DEMO_DB_SERVICE:-postgres}"
DB_NAME="${DEMO_DB_NAME:-${POSTGRES_DB:-warehouse_flow_manager}}"
DB_USER="${DEMO_DB_USERNAME:-${POSTGRES_USER:-postgres}}"

echo "Loading optional demo seed data..."
echo "SQL file: $SEED_FILE"
echo "Database service: $DB_SERVICE"
echo "Database name: $DB_NAME"
echo "Database user: $DB_USER"

cd "$ROOT_DIR"

docker compose --env-file "$ENV_FILE" exec -T "$DB_SERVICE" \
  psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 < "$SEED_FILE"

echo "Demo seed data loaded successfully."