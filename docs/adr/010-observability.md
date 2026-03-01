# ADR-010: Observability strategy

- **Status:** Accepted
- **Date:** Milestone 0

## Context

A distributed, event-driven system is difficult to operate without the three
pillars of observability: structured logs, metrics, and distributed traces. A
single business workflow crosses multiple services and Kafka, so correlation
across boundaries is essential.

## Decision

Implement all three pillars:

### Logging

- Structured JSON logs outside local developer mode.
- Each log entry includes: service name, trace id, span id, correlation id,
  event id where applicable.
- No secrets or full tokens; sanitized personal data.

### Metrics

Expose metrics via Micrometer and Prometheus for:

- HTTP request count and latency
- Error rates
- Kafka messages produced and consumed
- Consumer failures
- Retry attempts
- Dead-lettered events
- Outbox backlog
- Order workflow completion duration
- Inventory rejection count
- Delivery failure count

### Tracing

Distributed tracing via OpenTelemetry, propagated through HTTP, database, outbox
publication, Kafka production, and consumer handling. Traces are exported via an
OpenTelemetry Collector to Jaeger (or Grafana Tempo).

### Tooling

- Prometheus for metrics scraping.
- Grafana for dashboards.
- OpenTelemetry Collector as the tracing pipeline.
- Local dashboards and instructions provided under `infrastructure/monitoring`.

## Alternatives considered

- **Logs only:** Insufficient for understanding distributed latency and
  event-flow.
- **Per-service APM agents without OTel:** Vendor lock-in and less consistent
  propagation.
- **Custom correlation middleware:** Re-invents what OpenTelemetry provides
  standard.

## Consequences

- **Positive:** A workflow can be followed across services using one correlation
  id; outbox backlog and DLT metrics are visible; latency and error hotspots are
  discoverable.
- **Negative:** Adds the monitoring stack to local startup (kept behind a
  profile) and instrumentation effort per service.
- **Neutral:** Correlation ids are threaded through the event envelope and
  propagated into MDC for logs.
