# ADR-005: Choreography-based Saga

- **Status:** Accepted
- **Date:** Milestone 0

## Context

Order fulfilment spans multiple services: order creation, inventory reservation,
delivery scheduling, and notification. A failure in one step (e.g., insufficient
stock, or delivery scheduling failure after reservation) must trigger
compensation in prior steps so the system ends in a consistent state.

Two common coordination approaches are **orchestration** (a central orchestrator
commands each step) and **choreography** (each service reacts to events and
emits new events).

## Decision

Use a **choreography-based saga**. Each service consumes domain events and
publishes its own events in response; there is no central orchestrator.

Examples:

- Successful: `OrderCreated` -> Inventory reserves -> `InventoryReserved` ->
  Order + Delivery react -> `DeliveryScheduled` -> `CONFIRMED`.
- Insufficient stock: `OrderCreated` -> Inventory rejects -> `InventoryRejected`
  -> Order `CANCELLED`.
- Delivery scheduling failure (after reservation): `DeliverySchedulingFailed` ->
  Order `CANCELLATION_PENDING` -> `InventoryReleaseRequested` ->
  `InventoryReleased` -> `OrderCancelled` -> Order `CANCELLED`.

Compensation rules:

- Compensation runs only for steps that succeeded.
- Inventory is released only if it was reserved.
- Delivery is cancelled only if it was scheduled/assigned.
- An order reaches `CANCELLED` only after required compensation succeeds.

## Alternatives considered

- **Orchestration (e.g., a saga orchestrator service):** Centralizes control and
  makes the workflow easier to follow, but adds a single point of coordination
  and a new service. Rejected to keep the initial architecture minimal and to
  demonstrate event-reactive design.
- **Distributed transactions (2PC):** Explicitly disallowed by the project
  principles; not viable across independent databases.

## Consequences

- **Positive:** No central orchestrator; services remain loosely coupled;
  natural fit with the event-driven backbone.
- **Negative:** The workflow is implicit and harder to follow; compensation
  logic is distributed; testing must cover each reaction.
- **Neutral:** Correlation IDs thread the workflow together for observability and
  debugging.
