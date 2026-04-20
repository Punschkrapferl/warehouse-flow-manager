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

APP_PORT="${SERVER_PORT:-8081}"

if docker compose "${COMPOSE_ARGS[@]}" ps --status running --services 2>/dev/null | grep -qx 'app'; then
  echo "Demo stack is already running."
  echo "App: http://localhost:${APP_PORT}"
  echo "Swagger UI: http://localhost:${APP_PORT}/swagger-ui.html"
  echo "Demo DB: localhost:${POSTGRES_PORT}"
  exit 0
fi

if lsof -nP -iTCP:"${APP_PORT}" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Error: port ${APP_PORT} is already in use."
  echo "Run: lsof -nP -iTCP:${APP_PORT} -sTCP:LISTEN"
  echo "Then stop the existing process and try again."
  exit 1
fi

echo "Starting local demo PostgreSQL database and app ..."
docker compose "${COMPOSE_ARGS[@]}" up -d --build --wait --wait-timeout 300

echo "Demo stack is running."
echo "App: http://localhost:${APP_PORT}"
echo "Swagger UI: http://localhost:${APP_PORT}/swagger-ui.html"
echo "Demo DB: localhost:${POSTGRES_PORT}"