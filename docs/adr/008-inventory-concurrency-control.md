# ADR-008: Concurrency control for inventory

- **Status:** Accepted
- **Date:** Milestone 0

## Context

Inventory reservations must never oversell stock, even under concurrent requests
for the same products. A reservation may span multiple products and must be
atomic: either all products are reserved or none are (no partial reservations).
Releasing a reservation more than once must be safe, and stock must never become
negative.

## Decision

Use a combination of:

1. **Pessimistic locking (row locks)** on the inventory rows for the products
   involved in a reservation, acquired within a single transaction. This
   serializes concurrent reservations touching overlapping products.
2. **Atomic multi-product reservation:** lock all relevant product inventory rows
   (ordered consistently, e.g., by product id, to avoid deadlocks), verify
   availability for all products, then apply all decrements or roll back
   entirely.
3. **Check constraints:** a database `CHECK` constraint enforcing
   `available_quantity >= 0` as a backstop against oversell.
4. **Idempotent release:** reservation releases are keyed by reservation id with
   a status guard so a repeated release is a no-op.
5. **Optimistic version fields** where appropriate for adjustment operations.

The chosen concurrency strategy is documented in the inventory service and this
ADR; the explicit strategy is **pessimistic row-level locking with ordered
acquisition plus a non-negative check constraint**.

## Alternatives considered

- **Optimistic locking only (version fields):** Higher retry rate under
  contention for popular products; can still allow oversell races if not paired
  with a check constraint.
- **Application-level in-memory locking:** Does not work across instances;
  rejected for a horizontally scalable service.
- **Event sourcing with a single writer:** Adds significant complexity; out of
  scope for the initial inventory model.

## Consequences

- **Positive:** Strong no-oversell guarantee; atomic multi-product reservations;
  safe duplicate releases.
- **Negative:** Pessimistic locks reduce concurrency for hot products; lock
  ordering must be consistent to avoid deadlocks.
- **Neutral:** The check constraint is a defense-in-depth backstop, not the
  primary mechanism.
