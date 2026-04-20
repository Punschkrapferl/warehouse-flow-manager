#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${PROJECT_ROOT}"

if [[ ! -f ".env.dev" ]]; then
  echo "Error: .env.dev file not found in project root."
  exit 1
fi

COMPOSE_ARGS=(-f docker-compose-dev.yml --env-file .env.dev)

echo "Loading local development environment from .env.dev ..."
set -a
source .env.dev
set +a

APP_PORT="${SERVER_PORT:-8080}"

if lsof -nP -iTCP:"${APP_PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Error: port ${APP_PORT} is already in use."
  echo "Run: lsof -nP -iTCP:${APP_PORT} -sTCP:LISTEN"
  echo "Then stop the existing process and try again."
  exit 1
fi

echo "Starting local dev PostgreSQL database ..."
docker compose "${COMPOSE_ARGS[@]}" up -d --wait --wait-timeout 120

echo "Starting Spring Boot with environment from .env.dev ..."
exec ./mvnw spring-boot:run