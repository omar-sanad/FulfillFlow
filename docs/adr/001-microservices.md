# ADR-001: Why microservices are used

- **Status:** Accepted
- **Date:** Milestone 0

## Context

FulfillFlow is a portfolio project intended to demonstrate reliable,
event-driven distributed-systems engineering. The core domain involves several
distinct responsibilities -- orders, inventory, delivery, and notifications --
that have different lifecycles, scaling needs, and failure characteristics.

The project's learning goals include independent data ownership, asynchronous
communication, saga compensation, and observability across service boundaries.

## Decision

Adopt a microservices architecture with four domain services initially:

- Order Service
- Inventory Service
- Delivery Service
- Notification Service

Services communicate asynchronously via Apache Kafka (domain events) and via
REST for client commands and synchronous queries. No separate configuration,
discovery, user, audit, or gateway microservices are introduced unless a
demonstrated requirement justifies them.

## Alternatives considered

- **Modular monolith:** A single deployable with internal modules. Simpler
  operationally, but does not exercise cross-service messaging, independent data
  ownership, or distributed saga -- which are core objectives of this portfolio.
- **Service-per-feature with shared database:** Would violate the
  database-ownership principle and undermine the demonstration of reliable
  distributed workflows.

## Consequences

- **Positive:** Each service can be developed, tested, and scaled independently.
  The architecture demonstrates real distributed-systems concerns (eventual
  consistency, compensation, observability).
- **Negative:** Increased operational complexity; more deployment units; network
  failure modes must be handled.
- **Neutral:** A monorepo is used to keep the codebase navigable while preserving
  deployment independence.
