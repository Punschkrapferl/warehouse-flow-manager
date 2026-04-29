#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${PROJECT_ROOT}"

if [[ ! -f ".env" ]]; then
  echo "Error: .env file not found in project root."
  exit 1
fi

COMPOSE_ARGS=(-f docker-compose.yml --env-file .env)

echo "Loading demo environment from .env ..."
set -a
source .env
set +a

echo "Starting local demo PostgreSQL database ..."
docker compose "${COMPOSE_ARGS[@]}" up -d postgres --wait --wait-timeout 120

echo "Running Maven test suite against demo PostgreSQL database ..."

# Maven runs on your host machine, not inside Docker.
# Therefore it must connect through the exposed host port, not through the Docker service name "postgres".
export DB_HOST="${TEST_DB_HOST:-localhost}"
export DB_PORT="${TEST_DB_PORT:-5433}"
export DB_NAME="${DB_NAME}"
export DB_USERNAME="${DB_USERNAME}"
export DB_PASSWORD="${DB_PASSWORD}"

exec ./mvnw clean test