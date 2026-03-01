# Architecture Decision Records

This directory holds FulfillFlow's Architecture Decision Records (ADRs). Each ADR
records a significant architectural decision, its context, alternatives
considered, and consequences.

## Index

| ADR | Title | Status |
|---|---|---|
| [ADR-001](001-microservices.md) | Why microservices are used | Accepted |
| [ADR-002](002-kafka-topic-and-partitioning.md) | Kafka topic and partitioning strategy | Accepted |
| [ADR-003](003-transactional-outbox.md) | Transactional outbox | Accepted |
| [ADR-004](004-idempotent-consumers.md) | Idempotent consumers | Accepted |
| [ADR-005](005-choreography-based-saga.md) | Choreography-based Saga | Accepted |
| [ADR-006](006-database-ownership.md) | Database ownership | Accepted |
| [ADR-007](007-keycloak-authentication.md) | Authentication with Keycloak | Accepted |
| [ADR-008](008-inventory-concurrency-control.md) | Concurrency control for inventory | Accepted |
| [ADR-009](009-retry-and-dead-letter.md) | Retry and dead-letter strategy | Accepted |
| [ADR-010](010-observability.md) | Observability strategy | Accepted |

## ADR template

Each ADR follows this structure:

- **Context** -- the problem and forces at play
- **Decision** -- the chosen approach
- **Alternatives considered** -- options rejected and why
- **Consequences** -- positive, negative, and neutral effects
- **Status** -- Proposed, Accepted, Deprecated, or Superseded
