# ADR-002: Kafka topic and partitioning strategy

- **Status:** Accepted
- **Date:** Milestone 0

## Context

The four services communicate through domain events. A consistent topic and
partitioning strategy is needed to provide per-aggregate ordering where required,
keep consumers isolated, and support retries and dead-lettering.

## Decision

Use **domain-based topics** with a versioned suffix:

```
orders.events.v1
inventory.events.v1
deliveries.events.v1
notifications.events.v1
```

- **Message key:** the aggregate identifier (e.g., `orderId`, `reservationId`,
  `deliveryId`) for the producing domain. This keeps events for a single
  aggregate in one partition, preserving per-aggregate order.
- **Partitioning:** default hash partitioning on the message key.
- **Consumer group:** one consumer group per consuming service per topic (e.g.,
  `order-service` consuming `inventory.events.v1`).
- **Retention:** default retention for domain topics (7 days) is acceptable for
  the portfolio; configurable.
- **Retry topics:** `<domain>.events.v1.retry` with a small number of attempt
  tiers.
- **Dead-letter topics:** `<domain>.events.v1.dlt` for events that exhaust
  retries.
- **Event ordering limitation:** ordering is guaranteed only within a single
  partition (i.e., per aggregate), not globally across aggregates or topics.

## Alternatives considered

- **One topic per event type:** Proliferates topics; makes consumer subscription
  management heavier without clear benefit.
- **Single global topic:** Loses per-aggregate ordering semantics and complicates
  consumer filtering.
- **Event-type-based partitioning:** Would not preserve per-aggregate order.

## Consequences

- **Positive:** Predictable ordering per aggregate; clear consumer isolation;
  straightforward retry/DLT naming.
- **Negative:** Cross-aggregate ordering is not guaranteed; consumers must be
  idempotent.
- **Neutral:** New event types within a domain share a topic and are
  differentiated by the `eventType` envelope field; schema compatibility rules
  apply.
