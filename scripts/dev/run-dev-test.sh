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

: "${POSTGRES_DB:?POSTGRES_DB is required in .env.dev}"
: "${POSTGRES_USER:?POSTGRES_USER is required in .env.dev}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required in .env.dev}"
: "${POSTGRES_PORT:?POSTGRES_PORT is required in .env.dev}"

: "${DB_HOST:?DB_HOST is required in .env.dev}"
: "${DB_PORT:?DB_PORT is required in .env.dev}"
: "${DB_NAME:?DB_NAME is required in .env.dev}"
: "${DB_USERNAME:?DB_USERNAME is required in .env.dev}"
: "${DB_PASSWORD:?DB_PASSWORD is required in .env.dev}"

echo "Starting local dev PostgreSQL database ..."
docker compose "${COMPOSE_ARGS[@]}" up -d --wait --wait-timeout 120

export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
export SPRING_DATASOURCE_USERNAME="${DB_USERNAME}"
export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}"
export SPRING_FLYWAY_URL="${SPRING_DATASOURCE_URL}"
export SPRING_FLYWAY_USER="${DB_USERNAME}"
export SPRING_FLYWAY_PASSWORD="${DB_PASSWORD}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-default}"
export SERVER_PORT="${SERVER_PORT:-8080}"

echo "Running tests with explicit dev datasource settings ..."
echo "Datasource URL: ${SPRING_DATASOURCE_URL}"
echo "Datasource User: ${SPRING_DATASOURCE_USERNAME}"

exec ./mvnw clean test