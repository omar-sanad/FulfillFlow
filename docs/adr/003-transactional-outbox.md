# ADR-003: Transactional outbox

- **Status:** Accepted
- **Date:** Milestone 0

## Context

When a service changes domain state and must publish an event, naive
"write-then-publish" creates dual-write problems: the database write may succeed
while the Kafka publish fails (or vice versa), leaving the system inconsistent.
Kafka and PostgreSQL do not share a transaction coordinator, so a two-phase
commit is not appropriate.

## Decision

Use the **transactional outbox pattern**. Each service writes its domain change
and a corresponding outbox row in the **same database transaction**. A separate
outbox publisher reads unpublished outbox rows and publishes them to Kafka,
marking them as published on success.

Each outbox record stores: `eventId`, `aggregateId`, `eventType`, `eventVersion`,
`payload`, `correlationId`, `causationId`, `occurredAt`, `producer`, and
publishing metadata.

## Alternatives considered

- **Dual-write with best-effort retry:** Inconsistent under partial failures;
  rejected.
- **Change Data Capture (CDC) via Debezium:** Viable and robust, but adds
  operational weight (Kafka Connect, Debezium connectors) that is heavier than
  needed for the portfolio. A polling outbox publisher is simpler and sufficient.
- **Kafka transactions only (no DB):** Does not keep the domain state and the
  event atomic with the database write.

## Consequences

- **Positive:** Domain state and outbox events are atomic; failed publication is
  recoverable because the outbox row persists.
- **Negative:** Adds a polling publisher and an outbox table to maintain; outbox
  backlog must be monitored.
- **Neutral:** The publisher must be at-least-once; consumers must be idempotent
  (see ADR-004).
