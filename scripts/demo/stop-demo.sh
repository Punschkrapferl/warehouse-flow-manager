#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${PROJECT_ROOT}"

if [[ ! -f ".env" ]]; then
  echo "Error: .env file not found in project root."
  exit 1
fi

COMPOSE_ARGS=(-f docker-compose.yml --env-file .env)

echo "Stopping local demo PostgreSQL database and app ..."
docker compose "${COMPOSE_ARGS[@]}" down