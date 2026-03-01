#!/usr/bin/env bash
# FulfillFlow one-time setup helper.
# Creates a local .env from .env.example if one does not exist and prints
# next-step guidance. Safe to re-run.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "[setup] Created .env from .env.example"
else
  echo "[setup] .env already exists; leaving it untouched"
fi

echo
echo "[setup] Next steps:"
echo "  make start          # start the local stack"
echo "  make start-infra    # start only Postgres, Kafka, Keycloak"
echo "  make status         # show running services"
echo
