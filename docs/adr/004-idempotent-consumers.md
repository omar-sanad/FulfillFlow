# ADR-004: Idempotent consumers

- **Status:** Accepted
- **Date:** Milestone 0

## Context

Kafka provides **at-least-once** delivery. Consumers may receive the same event
more than once (e.g., after a rebalance or a retry). Without idempotent
processing, duplicate delivery could create duplicate reservations, duplicate
deliveries, or duplicate notifications.

## Decision

Use an **inbox table** (consumed-event ledger) in each consuming service. Before
processing an event, the consumer attempts to record the event's `eventId` in its
inbox table under a unique constraint. If the insert succeeds, the event is
processed within the same transaction as its side effects. If the insert fails
with a unique-constraint violation, the event is a duplicate and is skipped.

Each service owns an `inbox_events` table with a unique constraint on
`eventId` (and optionally `(consumerGroup, eventId)`).

The actual guarantee is: **at-least-once delivery combined with idempotent
processing and local transactional consistency.** The project does not claim
global exactly-once delivery.

## Alternatives considered

- **Application-level deduplication caches:** Less durable; lost on restart;
  rejected for critical workflows.
- **Kafka transactions with read-process-write:** Achieves stronger exactly-once
  semantics for Kafka-to-Kafka pipelines, but our side effects involve a
  database, so the inbox approach is simpler and sufficient.

## Consequences

- **Positive:** Duplicate events produce no duplicate business effect; replay is
  safe.
- **Negative:** Each consumer carries an inbox table and deduplication logic.
- **Neutral:** Outbox publication and inbox consumption are symmetric halves of
  the reliability model.
