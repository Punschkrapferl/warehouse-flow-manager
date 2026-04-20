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

echo "Starting local dev PostgreSQL database ..."
docker compose "${COMPOSE_ARGS[@]}" up -d --wait --wait-timeout 120

echo "Running tests with environment from .env.dev ..."
exec ./mvnw clean test