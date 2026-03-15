# FulfillFlow Makefile
# Provides a small, documented command surface for local development.
#
# Targets:
#   make setup    - one-time preparation (copy .env, prepare tooling)
#   make start    - start the local stack (infra + services + frontend)
#   make stop     - stop the local stack
#   make test     - run backend and frontend tests
#   make clean    - remove generated artifacts (warns before deleting data)
#   make status   - show running containers
#   make logs     - tail compose logs
#   make topics   - list Kafka topics
#   make help     - show this help

.DEFAULT_GOAL := help

COMPOSE ?= docker compose
COMPOSE_FILE ?= compose.yaml
ENV_FILE ?= .env

.PHONY: help setup start start-infra start-monitoring stop test clean status logs topics

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

setup: ## One-time preparation: create .env from example if missing
	@if [ ! -f $(ENV_FILE) ]; then \
		echo "Creating $(ENV_FILE) from .env.example"; \
		cp .env.example $(ENV_FILE); \
		echo "Edit $(ENV_FILE) if needed, then run 'make start'"; \
	else \
		echo "$(ENV_FILE) already exists"; \
	fi

# Start the full stack. The monitoring profile is optional.
start: ## Start the full local stack (infra + services + frontend)
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) --profile services --profile frontend up -d

start-infra: ## Start only the infrastructure (Postgres, Kafka, Keycloak)
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

start-monitoring: ## Start the monitoring stack (Prometheus, Grafana, Jaeger)
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) --profile monitoring up -d

stop: ## Stop the local stack (keeps volumes/data)
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) --profile services --profile frontend --profile monitoring down

test: ## Run backend unit tests
	mvn -B -ntp test

test-frontend: ## Run frontend tests
	cd frontend && npm run test -- --run

status: ## Show running compose services
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) ps

logs: ## Tail compose logs
	$(COMPOSE) --env-file $(ENV_FILE) -f $(COMPOSE_FILE) logs -f --tail=100

topics: ## List Kafka topics (requires Kafka to be running)
	@echo "Kafka topic inspection is wired up in the infrastructure milestone."

# clean removes generated artifacts. It does NOT delete persistent data
# automatically -- it warns and requires confirmation.
clean: ## Remove generated build artifacts (warns before touching data)
	@echo "This target removes generated build artifacts."
	@echo "It will NOT delete Docker volumes (persistent data) automatically."
	@echo "To remove persistent data, run: docker compose down -v"
	@find . -type d -name target -prune -exec rm -rf {} + 2>/dev/null || true
	@find . -type d -name node_modules -prune -exec rm -rf {} + 2>/dev/null || true
	@find . -type d -name dist -prune -exec rm -rf {} + 2>/dev/null || true
	@find . -type d -name build -prune -exec rm -rf {} + 2>/dev/null || true
	@echo "Generated artifacts removed."
