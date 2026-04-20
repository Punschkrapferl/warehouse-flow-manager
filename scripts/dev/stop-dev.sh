#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${PROJECT_ROOT}"

if [[ ! -f ".env.dev" ]]; then
  echo "Error: .env.dev file not found in project root."
  exit 1
fi

COMPOSE_ARGS=(-f docker-compose-dev.yml --env-file .env.dev)

echo "Stopping local dev PostgreSQL database ..."
docker compose "${COMPOSE_ARGS[@]}" down