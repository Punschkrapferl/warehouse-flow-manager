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

echo "Running tests with environment from .env ..."
exec ./mvnw clean test