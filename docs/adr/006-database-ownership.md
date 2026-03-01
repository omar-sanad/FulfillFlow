# ADR-006: Database ownership

- **Status:** Accepted
- **Date:** Milestone 0

## Context

Shared databases create hidden coupling between services, allow schema changes
in one service to break another, and undermine independent deployment. The
project principles require that each service own its data and that no service
reads another service's database.

## Decision

Each service owns its own PostgreSQL database (or logically isolated schema with
its own credentials) and is the sole writer and reader of that data:

- Order Service owns the order database.
- Inventory Service owns the inventory database.
- Delivery Service owns the delivery database.
- Notification Service owns the notification database.

Cross-service data needs are satisfied through events and APIs, never through
direct database access.

## Alternatives considered

- **Shared database, separate schemas:** Still allows accidental cross-service
  queries; weaker isolation. Used only if a single PostgreSQL instance is
  necessary for local footprint, with strict logical separation and distinct
  credentials per service.
- **Shared tables:** Rejected; maximal coupling.

## Consequences

- **Positive:** Strong encapsulation; independent schema evolution; services can
  be deployed and scaled independently.
- **Negative:** Some data duplication across services (e.g., a delivery service
  may hold a snapshot of order information it needs); eventual consistency
  between services.
- **Neutral:** In local development, databases may share a single PostgreSQL
  host for convenience while remaining logically isolated.
