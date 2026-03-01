# ADR-009: Retry and dead-letter strategy

- **Status:** Accepted
- **Date:** Milestone 0

## Context

Event consumers may fail transiently (e.g., a downstream dependency is briefly
unavailable) or permanently (e.g., a poison message). Without a retry and
dead-letter strategy, poison messages can block a partition, and transient
failures can cause unnecessary saga failures.

## Decision

Implement a layered retry and dead-letter strategy:

1. **In-process retry with exponential backoff:** a small number of immediate
   retries within the consumer for transient errors.
2. **Retry topics:** events that fail in-process are forwarded to a
   `<domain>.events.v1.retry` topic with exponential backoff between attempts,
   for a bounded number of attempts.
3. **Dead-letter topic:** events that exhaust retries are forwarded to
   `<domain>.events.v1.dlt` with structured error metadata (error type, message,
   stacktrace hash, attempt count, timestamps).
4. **Authorized inspection:** DLT events are inspectable via an
   administrator-only API and operational UI.
5. **Controlled replay:** an administrator can replay a recoverable DLT event
   back onto its original topic. Replay remains idempotent via the inbox table
   (ADR-004), so a replayed event that was already processed is a no-op.

## Alternatives considered

- **No retries, immediate DLT:** Too aggressive for transient failures.
- **Unlimited retries:** Can block progress on poison messages indefinitely.
- **Blocking the partition on failure:** Harms throughput for the whole
  partition.

## Consequences

- **Positive:** Transient failures recover automatically; poison messages are
  quarantined, not lost; replay is safe and auditable.
- **Negative:** More topics and operational surface; DLT monitoring is required.
- **Neutral:** Retry count and backoff are configurable per topic/consumer.
